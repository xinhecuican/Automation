package tech.xinhecuican.automation.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import tech.xinhecuican.automation.AccessService;
import tech.xinhecuican.automation.R;
import tech.xinhecuican.automation.model.Storage;
import tech.xinhecuican.automation.utils.ToastUtil;
import tech.xinhecuican.automation.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    private ViewPager2 viewPager;
    private ImageView accessView;
    private ImageView powerView;
    private Context context;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = inflater.getContext();
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        ImageButton accessibilitySettingButton = view.findViewById(R.id.accessibility_setting);
        accessibilitySettingButton.setOnClickListener(v -> {
            startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 0);
        });
        accessView = view.findViewById(R.id.accessibility_state);
        if(Utils.isAccessibilitySettingsOn(inflater.getContext(), AccessService.class)){
            accessView.setImageResource(R.drawable.ok);
        }else{
            accessView.setImageResource(R.drawable.close);
        }
        ImageButton powerButton = view.findViewById(R.id.power_setting);
        powerButton.setOnClickListener(v->{
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + inflater.getContext().getPackageName()));
                startActivityForResult(intent, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        powerView = (ImageView)view.findViewById(R.id.power_state);
        if(Utils.isIgnoringBatteryOptimizations(inflater.getContext())){
            powerView.setImageResource(R.drawable.ok);
        }else{
            powerView.setImageResource(R.drawable.close);
        }

        Switch switchMain = (Switch) view.findViewById(R.id.switch_open);
        switchMain.setChecked(Storage.instance().isOpen());
        switchMain.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                if(!Utils.isAccessibilitySettingsOn(context, AccessService.class)){
                    ToastUtil.ToastShort(context, context.getString(R.string.accessiblity_not_open));
                    accessibilitySettingButton.startAnimation(AnimationUtils.loadAnimation(context, R.anim.hint));
                    switchMain.setChecked(false);
                }
                else if(!Utils.isIgnoringBatteryOptimizations(context)){
                    ToastUtil.ToastShort(context, context.getString(R.string.power_not_open));
                    powerButton.startAnimation(AnimationUtils.loadAnimation(context, R.anim.hint));
                    switchMain.setChecked(false);
                }
                else{
                    Storage.instance().setOpen(true);
                    Intent serviceIntent = new Intent(context, AccessService.class);
                    serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startService(serviceIntent);
                }
            }
            else{
                Storage.instance().setOpen(false);
                try {
                    AccessService.getInstance().stopService();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        Switch switchSuspend = (Switch)view.findViewById(R.id.switch_suspend);
        switchSuspend.setChecked(Storage.instance().isShowBall());
        switchSuspend.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                if(!Utils.isAccessibilitySettingsOn(context, AccessService.class)) {
                    ToastUtil.ToastShort(context, context.getString(R.string.accessiblity_not_open));
                    accessibilitySettingButton.startAnimation(AnimationUtils.loadAnimation(context, R.anim.hint));
                    return;
                }
                AccessService service = AccessService.getInstance();
                if(service != null){
                    service.showSuspendball(() -> switchSuspend.setChecked(Storage.instance().isShowBall()));
                }
                Storage.instance().setShowBall(true);
            }
            else{
                Storage.instance().setShowBall(false);
                AccessService service = AccessService.getInstance();
                if(service != null){
                    service.removeSuspendView();
                }
            }
        });

        Switch switchTray = (Switch) view.findViewById(R.id.switch_hide_tray);
        switchTray.setChecked(Storage.instance().isHideTray());
        switchTray.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Storage.instance().setHideTray(isChecked);
            ((AppCompatActivity)context).finish();
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch(requestCode){
            case 0:
                if(Utils.isAccessibilitySettingsOn(context, AccessService.class)){
                    accessView.setImageResource(R.drawable.ok);
                }else{
                    accessView.setImageResource(R.drawable.close);
                }
                break;
            case 1:
                if(Utils.isIgnoringBatteryOptimizations(context)){
                    powerView.setImageResource(R.drawable.ok);
                }else{
                    powerView.setImageResource(R.drawable.close);
                }
                break;
        }
    }
}