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
import tech.xinhecuican.automation.model.ConditionModel;
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
        index = intent.getIntExtra("index", 0);
        isNew = index == -1;
        if(isNew){
            operation = new Operation((getString(R.string.operation)
                    .concat(String.valueOf(Storage.instance().getOperations().size()))));
        }
        else{
            operation = Storage.instance().getOperation(index);
        }
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
                            setBackData(false);
                            OperationActivity.this.finish();
                        });
                normalDialog.setNegativeButton(R.string.close,
                        (dialog, which) -> {
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
                service.showActivityNameDialog(new AccessService.WindowInfoResultListener() {
                    @Override
                    public void onWidgetResult(WidgetDescription description) {
                        if(description != null) {
                            operation.setActivityName(description.activityName);
                            operation.setPackageName(description.packageName);
                            activityDescription.setText(Utils.showActivityName(operation.getPackageName(), operation.getActivityName()));
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
        activityDescription.setText(Utils.showActivityName(operation.getPackageName(), operation.getActivityName()));

        RadioButton isAutoButton = (RadioButton) findViewById(R.id.allow_auto);
        isAutoButton.setChecked(operation.isAuto());
        isAutoButton.setOnClickListener(new View.OnClickListener() {
            boolean isCheck = isAutoButton.isChecked();
            @Override
            public void onClick(View v) {
                isCheck = !isCheck;
                operation.setAuto(isCheck);
                isChange = true;
                isAutoButton.setChecked(isCheck);
            }
        });

        RadioButton onePageButton = (RadioButton) findViewById(R.id.one_page);
        onePageButton.setChecked(operation.isOnePage());
        onePageButton.setOnClickListener(new View.OnClickListener() {
            boolean isCheck = onePageButton.isChecked();
            @Override
            public void onClick(View v) {
                isCheck = !isCheck;
                operation.setOnePage(isCheck);
                isChange = true;
                onePageButton.setChecked(isCheck);
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
        treeView.setTreeViewControlListener(new TreeViewControlListener() {
            @Override
            public void onScaling(int state, int percent) {

            }

            @Override
            public void onDragMoveNodesHit(@Nullable NodeModel<?> draggingNode, @Nullable NodeModel<?> hittingNode, @Nullable View draggingView, @Nullable View hittingView) {
            }

            @Override
            public boolean onDragMoveResult(@Nullable NodeModel<?> draggingNode, @Nullable NodeModel<?> hittingNode) {
                boolean accept = draggingNode != rootNode && hittingNode != draggingNode.getParentNode();
                if(accept){
                    if(hittingNode.getValue() instanceof ModelGroup) {
                        ModelGroup group = (ModelGroup) hittingNode.getValue();
                        ModelGroup parent = (ModelGroup) draggingNode.getParentNode().getValue();
                        Model current = (Model) draggingNode.getValue();
                        parent.removeModel(current);
                        group.addModel(current);
                        isChange = true;
                    }
                    else if(hittingNode.getValue() instanceof ConditionModel){
                        ConditionModel conditionModel = (ConditionModel)hittingNode.getValue();
                        accept = conditionModel.addModel((Model) draggingNode.getValue());
                        if(accept){
                            if(draggingNode.getParentNode().getValue() instanceof ModelGroup){
                                ModelGroup parent = (ModelGroup)draggingNode.getParentNode().getValue();
                                parent.removeModel((Model) draggingNode.getValue());
                            }
                            if(draggingNode.getParentNode().getValue() instanceof ConditionModel){
                                ConditionModel parent = (ConditionModel) draggingNode.getParentNode().getValue();
                                parent.removeModel((Model) draggingNode.getValue());
                            }
                            isChange = true;
                        }
                    }
                    else
                        accept = false;
                }
                return accept;
            }

            @Override
            public void onDragMoveIndexChange(@Nullable NodeModel<?> draggingNode, int from, int to) {
                if(draggingNode.getParentNode().getValue() instanceof ModelGroup) {
                    operation.moveModel((ModelGroup) draggingNode.getParentNode().getValue(), from, to);
                    isChange = true;
                }
                if(draggingNode.getParentNode().getValue() instanceof ConditionModel){
                    ConditionModel conditionModel = (ConditionModel) draggingNode.getParentNode().getValue();
                    conditionModel.exchangeModel();
                    isChange = true;
                }
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
                isChange = true;
                treeEditor.addChildNodes(rootNode, new NodeModel<Model>(modelGroup));
            });
            Button delayModelButton = bottomSheet.findViewById(R.id.new_model_delay);
            assert delayModelButton != null;
            delayModelButton.setOnClickListener(v->{
                DelayModel delayModel = new DelayModel();
                operation.addModel(delayModel);
                isChange = true;
                treeEditor.addChildNodes(rootNode, new NodeModel<Model>(delayModel));
            });
            Button conditionModelButton = bottomSheet.findViewById(R.id.new_condition_model);
            assert conditionModelButton != null;
            conditionModelButton.setOnClickListener(v->{
                ConditionModel conditionModel = new ConditionModel();
                operation.addModel(conditionModel);
                isChange = true;
                treeEditor.addChildNodes(rootNode, new NodeModel<Model>(conditionModel));
            });
            bottomSheet.show();//显示弹窗
        });

        Switch deleteModelSwitch = (Switch)findViewById(R.id.delete_model_switch);
        deleteModelSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> treeModelAdapter.setDelete(isChecked));

        treeEditor.requestMoveNodeByDragging(false);
        Switch editModeSwitch = (Switch)findViewById(R.id.edit_model_switch);
        editModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            treeEditor.requestMoveNodeByDragging(isChecked);
            treeModelAdapter.setExpandable(!isChecked);
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
        bundle.putInt("index", index==-1&&isChange?Storage.instance().operationCount():index);
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
        Model model = rootNode.getValue();
        if(model.getModelType() == 2) {
            for (Model childModel : ((ModelGroup) rootNode.getValue()).getModels()) {
                buildNodeModel(tree, rootNode, childModel);
            }
        }
        else if(model.getModelType() == 4){
            ConditionModel conditionModel = (ConditionModel) model;
            if(conditionModel.getSuccessModel() != null){
                buildNodeModel(tree, rootNode, conditionModel.getSuccessModel());
            }
            if(conditionModel.getFailModel() != null){
                buildNodeModel(tree, rootNode, conditionModel.getFailModel());
            }
        }
    }

    private void buildNodeModel(TreeModel<Model> tree, NodeModel<Model> rootNode, Model model){
        if (model.getModelType() == 2 || model.getModelType() == 4) {
            NodeModel<Model> nodeModel = new NodeModel<Model>(model);
            tree.addNode(rootNode, nodeModel);
            buildTreeModel(tree, nodeModel);
        } else {
            tree.addNode(rootNode, new NodeModel<Model>(model));
        }
    }

}