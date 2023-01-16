package tech.xinhecuican.automation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.gyso.treeview.GysoTreeView;
import com.gyso.treeview.TreeViewEditor;
import com.gyso.treeview.layout.BoxRightTreeLayoutManager;
import com.gyso.treeview.layout.TreeLayoutManager;
import com.gyso.treeview.line.DashLine;
import com.gyso.treeview.listener.TreeViewControlListener;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.model.TreeModel;

import tech.xinhecuican.automation.adapter.TreeModelAdapter;
import tech.xinhecuican.automation.manager.OperationManager;
import tech.xinhecuican.automation.model.ClickModel;
import tech.xinhecuican.automation.model.CoordinateDescription;
import tech.xinhecuican.automation.model.DelayModel;
import tech.xinhecuican.automation.model.Model;
import tech.xinhecuican.automation.model.ModelGroup;
import tech.xinhecuican.automation.model.Operation;
import tech.xinhecuican.automation.model.ScrollModel;
import tech.xinhecuican.automation.model.Storage;
import tech.xinhecuican.automation.model.WidgetDescription;
import tech.xinhecuican.automation.utils.ToastUtil;
import tech.xinhecuican.automation.utils.Utils;

public class OperationActivity extends AppCompatActivity {
    private Operation operation;
    private int index;
    private boolean isNew;
    private boolean isChange;
    private AccessService service;
    private TextView activityDescription;
    private TreeModelAdapter treeModelAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        service = AccessService.getInstance();

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

        ImageButton operationPackageButton = (ImageButton) findViewById(R.id.widget_picker);
        operationPackageButton.setOnClickListener(view->{
            if(service != null){
                service.setDescription(operation.generateCoordDescription(-1), operation.generateWidgetDescription(-1));
                service.showActivityCustomizationDialog(false, new AccessService.WindowInfoResultListener() {
                    @Override
                    public void onWidgetResult(WidgetDescription description) {
                        if(description != null) {
                            operation.setActivityName(description.activityName);
                            operation.setPackageName(description.packageName);
                            activityDescription.setText(operation.getActivityName());
                            isChange = true;
                        }
                        Utils.setTopApp(OperationActivity.this);
                    }

                    @Override
                    public void onCoordResult(CoordinateDescription description) {
                    }
                });
            }
            else{
                ToastUtil.ToastShort(this, getString(R.string.accessiblity_not_open));
            }
        });

        activityDescription = (TextView) findViewById(R.id.view_description);
        activityDescription.setText(operation.getActivityName());

        RadioButton isAutoButton = (RadioButton) findViewById(R.id.allow_auto);
        isAutoButton.setChecked(operation.isAuto());
        isAutoButton.setOnClickListener(new View.OnClickListener() {
            boolean isCheck = false;
            @Override
            public void onClick(View v) {
                isCheck = !isCheck;
                operation.setAuto(isCheck);
                isChange = true;
                isAutoButton.setChecked(isCheck);
            }
        });

        GysoTreeView treeView = (GysoTreeView) findViewById(R.id.tree_list);
        int space_50dp = 50;
        int space_20dp = 20;
//choose a demo line or a customs line. StraightLine, PointedLine, DashLine, SmoothLine are available.
        DashLine line =  new DashLine(Color.parseColor("#4DB6AC"),8);
//choose layoout manager. VerticalTreeLayoutManager,RightTreeLayoutManager are available.
        TreeLayoutManager treeLayoutManager = new BoxRightTreeLayoutManager(this,space_50dp,space_20dp,line);

        treeModelAdapter = new TreeModelAdapter(this, operation.getRootModel(), treeView.getEditor(), treeView);
        NodeModel<Model> rootNode = new NodeModel<Model>(operation.getRootModel());
        TreeModel<Model> treeModel = new TreeModel<>(rootNode);
        buildTreeModel(treeModel, rootNode);
        treeView.setAdapter(treeModelAdapter);
        treeView.setTreeLayoutManager(treeLayoutManager);
        treeModelAdapter.setTreeModel(treeModel);
        TreeViewEditor treeEditor = treeView.getEditor();
        treeEditor.requestMoveNodeByDragging(true);
        treeView.requestDisallowInterceptTouchEvent(false);
        treeView.setTreeViewControlListener(new TreeViewControlListener() {
            @Override
            public void onScaling(int state, int percent) {

            }

            @Override
            public void onDragMoveNodesHit(@Nullable NodeModel<?> draggingNode, @Nullable NodeModel<?> hittingNode, @Nullable View draggingView, @Nullable View hittingView) {

            }
        });

        ImageButton newModelButton = (ImageButton) findViewById(R.id.new_model);
        newModelButton.setOnClickListener(view->{
            BottomSheetDialog bottomSheet = new BottomSheetDialog(this);//实例化BottomSheetDialog
            bottomSheet.setCancelable(true);//设置点击外部是否可以取消
            bottomSheet.setContentView(R.layout.new_model_dialog);//设置对框框中的布局
            Button clickModelButton = bottomSheet.findViewById(R.id.new_click_model);
            assert clickModelButton != null;
            clickModelButton.setOnClickListener(view1->{
                ClickModel clickModel = new ClickModel();
                operation.addModel(clickModel);
                treeEditor.addChildNodes(rootNode, new NodeModel<Model>(clickModel));
                isChange = true;
            });
            Button scrollModelButton = bottomSheet.findViewById(R.id.new_scroll_model);
            assert scrollModelButton != null;
            scrollModelButton.setOnClickListener(v->{
                ScrollModel scrollModel = new ScrollModel();
                operation.addModel(scrollModel);
                treeEditor.addChildNodes(rootNode, new NodeModel<Model>(scrollModel));
                isChange = true;
            });
            Button groupModelButton = bottomSheet.findViewById(R.id.new_group_model);
            assert groupModelButton != null;
            groupModelButton.setOnClickListener(v->{
                ModelGroup modelGroup = new ModelGroup();
                operation.addModel(modelGroup);
                treeEditor.addChildNodes(rootNode, new NodeModel<Model>(modelGroup));
            });
            Button delayModelButton = bottomSheet.findViewById(R.id.new_model_delay);
            assert delayModelButton != null;
            delayModelButton.setOnClickListener(v->{
                DelayModel delayModel = new DelayModel();
                operation.addModel(delayModel);
                treeEditor.addChildNodes(rootNode, new NodeModel<Model>(delayModel));
            });
            bottomSheet.show();//显示弹窗
        });

        Switch deleteModelSwitch = (Switch)findViewById(R.id.delete_model_switch);
        deleteModelSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                treeModelAdapter.setDelete(isChecked);
            }
        });

    }

    private boolean calcIsChange(){
        return treeModelAdapter.isChange() || isNew || isChange;
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
        super.onDestroy();
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

    private void buildTreeModel(TreeModel<Model> tree, NodeModel<Model> rootNode){
        for(Model childModel : ((ModelGroup)rootNode.getValue()).getModels()){
            if(childModel.getModelType() == 2){
                buildTreeModel(tree, new NodeModel<Model>(childModel));
            }else{
                tree.addNode(rootNode, new NodeModel<Model>(childModel));
            }
        }
    }

}