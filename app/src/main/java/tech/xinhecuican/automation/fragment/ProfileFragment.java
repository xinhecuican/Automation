package tech.xinhecuican.automation.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadioButton;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import tech.xinhecuican.automation.MainActivity;
import tech.xinhecuican.automation.OperationActivity;
import tech.xinhecuican.automation.R;
import tech.xinhecuican.automation.adapter.ItemTouchHelperCallback;
import tech.xinhecuican.automation.adapter.OperationAdapter;
import tech.xinhecuican.automation.manager.OperationManager;
import tech.xinhecuican.automation.model.Storage;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private OperationAdapter adapter;
    private boolean isDeleteViewShow;
    private RecyclerView recyclerView;
    private SearchView searchView;

    public ProfileFragment() {
        // Required empty public constructor
        isDeleteViewShow = false;
    }

    public void itemChanged(int index){
        adapter.notifyItemChanged(index);
    }

    public boolean isDeleteViewShow() {
        return isDeleteViewShow;
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = inflater.inflate(R.layout.fragment_profile, container, false);
        recyclerView = mainView.findViewById(R.id.main_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext(), RecyclerView.VERTICAL, true));
        if(adapter != null){
            OperationManager.instance().removeListener(adapter);
        }
        adapter = new OperationAdapter((AppCompatActivity)inflater.getContext(), Storage.instance().getOperations()){
            @Override
            public void onItemLongClick(View view, int position) {
                if(!isDeleteViewShow) {
                    ((RadioButton) view.findViewById(R.id.operation_option_button)).setChecked(true);
                    OperationManager.instance().addDelete(position);
                    ((MainActivity) inflater.getContext()).changeDeleteView(false);
                    isDeleteViewShow = true;
                }
            }
        } ;
        ItemTouchHelper.Callback itemTouchHelperCallback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        recyclerView.setAdapter(adapter);
        touchHelper.attachToRecyclerView(recyclerView);

        FloatingActionButton floatingActionButton = (FloatingActionButton) mainView.findViewById(R.id.new_operation);
        floatingActionButton.setOnClickListener(view1 -> {
            Intent intent = new Intent(inflater.getContext(), OperationActivity.class);
            intent.putExtra("index", -1);
            startActivityForResult(intent, 0);
        });

        searchView = (SearchView) mainView.findViewById(R.id.searchbar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 得到输入管理对象
                InputMethodManager imm = (InputMethodManager) inflater.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    // 这将让键盘在所有的情况下都被隐藏，但是一般我们在点击搜索按钮后，输入法都会乖乖的自动隐藏的。
                    imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0); // 输入法如果是显示状态，那么就隐藏输入法                    }
                    searchView.clearFocus(); // 不获取焦点
                    return true;
                }
                searchView.clearFocus(); // 不获取焦点
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                int position = OperationManager.instance().getSearchPostion(newText);
                if(position != -1){
                    recyclerView.scrollToPosition(position);
                }
                return false;
            }
        });
        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        OperationManager.instance().addListener(adapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        OperationManager.instance().removeListener(adapter);
    }

    public void showSelectButtons(){
        for(int i=0; i< recyclerView.getChildCount(); i++){
            recyclerView.getChildAt(i).findViewById(R.id.operation_option_button).setVisibility(View.VISIBLE);
        }
    }

    public void hideSelectButtons(){
        for(int i=0; i< recyclerView.getChildCount(); i++){
            RadioButton button = (RadioButton)recyclerView.getChildAt(i).findViewById(R.id.operation_option_button);
            button.setChecked(false);
            button.setVisibility(View.GONE);
        }
        OperationManager.instance().cancelDelete();
        isDeleteViewShow = false;
    }

    public void enableAllSelectButton(){
        for(int i=0; i< recyclerView.getChildCount(); i++){
            RadioButton button = (RadioButton)recyclerView.getChildAt(i).findViewById(R.id.operation_option_button);
            button.setChecked(true);
        }
    }

    public void clearFocus(){
        searchView.clearFocus();
    }

    public boolean hasFocus(){
        return searchView.hasFocus();
    }
}