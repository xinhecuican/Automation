package tech.xinhecuican.automation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.lang.reflect.InvocationTargetException;

import tech.xinhecuican.automation.adapter.ModelAdapter;
import tech.xinhecuican.automation.manager.OperationManager;
import tech.xinhecuican.automation.model.ClickModel;
import tech.xinhecuican.automation.model.CoordinateDescription;
import tech.xinhecuican.automation.model.Model;
import tech.xinhecuican.automation.model.Operation;
import tech.xinhecuican.automation.model.ScrollModel;
import tech.xinhecuican.automation.model.Storage;
import tech.xinhecuican.automation.model.WidgetDescription;
import tech.xinhecuican.automation.utils.Debug;
import tech.xinhecuican.automation.utils.ToastUtil;
import tech.xinhecuican.automation.utils.Utils;

public class OperationActivity extends AppCompatActivity implements AccessService.WindowInfoResultListener {
    private Operation operation;
    private int index;
    private boolean isNew;
    private ModelAdapter adapter;
    private boolean isChange;
    private AccessService service;
    private TextView activityDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        service = AccessService.getInstance();
        if(service != null) {
            service.addWindowInfoResultListener(this);
        }

        isChange = false;
        setContentView(R.layout.activity_operation);
        Intent intent = getIntent();
        operation = (Operation)intent.getSerializableExtra("operation");
        index = intent.getIntExtra("index", 0);
        isNew = index == -1;
        TextInputEditText textEdit = (TextInputEditText) findViewById(R.id.operation_name_edit);
        textEdit.setText(operation.getName());
        textEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                operation.setName(String.valueOf(editable));
            }
        });

        adapter = new ModelAdapter(operation.getModels(), this, operation);
        RecyclerView recyclerView = findViewById(R.id.model_list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            if(calcIsChange())
            {
                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(OperationActivity.this);
                normalDialog.setTitle(R.string.dialog_back_name);
                normalDialog.setMessage(R.string.dialog_back_info);
                normalDialog.setPositiveButton(R.string.confirm,
                        (dialog, which) -> {
                            setBackData(true);
                            OperationActivity.this.finish();
                        });
                normalDialog.setNegativeButton(R.string.close,
                        (dialog, which) -> {
                            setBackData(false);
                            OperationActivity.this.finish();
                        });
                normalDialog.show();
            }
            else
            {
                setBackData(false);
                OperationActivity.this.finish();
            }
        });

        ImageButton confirmButton = (ImageButton) findViewById(R.id.confirm);
        confirmButton.setOnClickListener(view -> {
            setBackData(true);
            OperationActivity.this.finish();
        });

        ImageButton newModelButton = (ImageButton) findViewById(R.id.new_model);
        newModelButton.setOnClickListener(view->{
            BottomSheetDialog bottomSheet = new BottomSheetDialog(this);//实例化BottomSheetDialog
            bottomSheet.setCancelable(true);//设置点击外部是否可以取消
            bottomSheet.setContentView(R.layout.new_model_dialog);//设置对框框中的布局
            Button clickModelButton = bottomSheet.findViewById(R.id.new_click_model);
            assert clickModelButton != null;
            clickModelButton.setOnClickListener(view1->{
                operation.addModel(new ClickModel());
                adapter.notifyItemChanged(operation.getModelCount());
                isChange = true;
            });
            Button scrollModelButton = bottomSheet.findViewById(R.id.new_scroll_model);
            assert scrollModelButton != null;
            scrollModelButton.setOnClickListener(v->{
                operation.addModel(new ScrollModel());
                adapter.notifyItemChanged(operation.getModelCount());
                isChange = true;
            });
            bottomSheet.show();//显示弹窗
        });

        ImageButton operationPackageButton = (ImageButton) findViewById(R.id.widget_picker);
        operationPackageButton.setOnClickListener(view->{
            if(service != null){
                service.setDescription(-1,
                        operation.generateCoordDescription(-1), operation.generateWidgetDescription(-1));
                service.showActivityCustomizationDialog();
            }
            else{
                ToastUtil.ToastShort(this, getString(R.string.accessiblity_not_open));
            }
        });

        activityDescription = (TextView) findViewById(R.id.view_description);
        activityDescription.setText(operation.getActivityName());

        RadioButton isAutoButton = (RadioButton) findViewById(R.id.allow_auto);
        isAutoButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                operation.setAuto(isChecked);
                isChange = true;
            }
        });
    }

    private boolean calcIsChange(){
        return adapter.isChange() || isNew || isChange;
    }

    private void setBackData(boolean isChange)
    {
        if(isChange)
        {
            if(isNew) OperationManager.instance().append(operation);
            else OperationManager.instance().change(operation, index);
        }
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index==-1?Storage.instance().operationCount():index);
        resultIntent.putExtras(bundle);
        setResult(0, resultIntent);
    }

    @Override
    public void onDestroy() {
        if(service != null)
            service.removeWindowInfoResultListener(this);
        super.onDestroy();
    }

    @Override
    public void onWidgetResult(int index, WidgetDescription description) {
        if(index != -1){
            Model model = operation.getModels().get(index);
            if(model.needWidgetDescription()){
                try {
                    model.getClass().getMethod("setWidgetDescription", WidgetDescription.class).invoke(model, description);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            adapter.notifyItemChanged(index);
        }
        operation.setActivityName(description.activityName);
        operation.setPackageName(description.packageName);
        activityDescription.setText(operation.getActivityName());
        isChange = true;
        Utils.setTopApp(this);
    }

    @Override
    public void onCoordResult(int index, CoordinateDescription description) {
        if(index != -1)
        {
            Model model = operation.getModels().get(index);
            try {
                model.getClass().getMethod("setX", int.class).invoke(model, description.x);
                model.getClass().getMethod("setY", int.class).invoke(model, description.y);
                isChange = true;
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                Debug.error(e.getMessage());
            }
            adapter.notifyItemChanged(index);
        }
    }
}