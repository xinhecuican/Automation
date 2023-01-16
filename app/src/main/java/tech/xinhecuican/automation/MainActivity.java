package tech.xinhecuican.automation;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import tech.xinhecuican.automation.adapter.MainPageAdapter;
import tech.xinhecuican.automation.databinding.ActivityMainBinding;
import tech.xinhecuican.automation.fragment.MainFragment;
import tech.xinhecuican.automation.fragment.ProfileFragment;
import tech.xinhecuican.automation.fragment.SettingFragment;
import tech.xinhecuican.automation.manager.OperationManager;
import tech.xinhecuican.automation.model.Storage;
import tech.xinhecuican.automation.utils.ToastUtil;
import tech.xinhecuican.automation.utils.Utils;

public class MainActivity extends AppCompatActivity{

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private List<Fragment> fragmentList;
    private ViewPager2 viewPager2;
    private boolean realBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Storage.instance().load();
        Storage.instance().checkOpen(Utils.isAccessibilitySettingsOn(this, AccessService.class)
                && Utils.isIgnoringBatteryOptimizations(this));
        if(Storage.instance().isOpen()) {
            Intent serviceIntent = new Intent(this, AccessService.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(serviceIntent);
        }
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        manager.getAppTasks().get(0).setExcludeFromRecents(Storage.instance().isHideTray());
        realBack = false;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fragmentList = new ArrayList<>();
        fragmentList.add(MainFragment.newInstance());
        fragmentList.add(ProfileFragment.newInstance());
        fragmentList.add(SettingFragment.newInstance());

        MainPageAdapter pageAdapter = new MainPageAdapter(this, fragmentList);
        viewPager2 = (ViewPager2)findViewById(R.id.view_pager);
        viewPager2.setAdapter(pageAdapter);
        viewPager2.setOffscreenPageLimit(1);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch(position){
                    case 0: tab.setText(R.string.main);break;
                    case 1: tab.setText(R.string.profile);break;
                    case 2: tab.setText(R.string.setting);break;
                }
            }
        });
        tabLayoutMediator.attach();

        RadioButton deleteButton = (RadioButton)findViewById(R.id.delete);
        deleteButton.setOnClickListener(v->{
            OperationManager.instance().delete();
        });
        RadioButton selectAllButton = (RadioButton)findViewById(R.id.select_all);
        selectAllButton.setOnClickListener(v->{
            OperationManager.instance().cancelDelete();
            for(int i=0; i<Storage.instance().operationCount(); i++){
                OperationManager.instance().addDelete(i);
            }
            ((ProfileFragment)fragmentList.get(1)).enableAllSelectButton();

        });

        AccessService service = AccessService.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu)
    {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        AccessService service;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null)
            return;
        ((ProfileFragment)fragmentList.get(1)).itemChanged(data.getIntExtra("index", 0));
    }

    public void changeDeleteView(boolean hidden){
        if(hidden){
            findViewById(R.id.delete_layout).setVisibility(View.GONE);
            findViewById(R.id.tab_layout).setVisibility(View.VISIBLE);
            ((ProfileFragment)fragmentList.get(1)).hideSelectButtons();
        }
        else{
            findViewById(R.id.delete_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.tab_layout).setVisibility(View.GONE);
            ((ProfileFragment)fragmentList.get(1)).showSelectButtons();
        }
    }

    @Override
    public void onBackPressed(){
        if(((ProfileFragment)fragmentList.get(1)).hasFocus())
            ((ProfileFragment)fragmentList.get(1)).clearFocus();
        else if(((ProfileFragment)fragmentList.get(1)).isDeleteViewShow()){
            changeDeleteView(true);
        }
        else{
            if(!realBack){
                realBack = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    ToastUtil.ToastBack(this, getString(R.string.confirm_back), new Toast.Callback() {
                        @Override
                        public void onToastHidden() {
                            realBack = false;
                        }
                    });
                }
            }
            super.onBackPressed();
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                //使EditText触发一次失去焦点事件
                v.setFocusable(false);
//                v.setFocusable(true); //这里不需要是因为下面一句代码会同时实现这个功能
                v.setFocusableInTouchMode(true);
                return true;
            }
        }
        return false;
    }

}