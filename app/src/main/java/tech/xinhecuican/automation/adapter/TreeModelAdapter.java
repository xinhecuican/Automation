package tech.xinhecuican.automation.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gyso.treeview.GysoTreeView;
import com.gyso.treeview.TreeViewEditor;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.model.NodeModel;

import java.lang.reflect.InvocationTargetException;

import tech.xinhecuican.automation.AccessService;
import tech.xinhecuican.automation.R;
import tech.xinhecuican.automation.listener.NumTextChangeListener;
import tech.xinhecuican.automation.model.ClickModel;
import tech.xinhecuican.automation.model.CoordinateDescription;
import tech.xinhecuican.automation.model.DelayModel;
import tech.xinhecuican.automation.model.Model;
import tech.xinhecuican.automation.model.ModelGroup;
import tech.xinhecuican.automation.model.ScrollModel;
import tech.xinhecuican.automation.model.WidgetDescription;
import tech.xinhecuican.automation.utils.Debug;
import tech.xinhecuican.automation.utils.ToastUtil;
import tech.xinhecuican.automation.utils.Utils;

public class TreeModelAdapter extends TreeViewBaseAdapter<Model>{
    private ModelGroup datas;
    private TreeViewEditor editor;
    private AppCompatActivity parent;
    private boolean isChange;
    private boolean isDelete;
    private GysoTreeView treeView;
    public TreeModelAdapter(AppCompatActivity parent, ModelGroup datas, TreeViewEditor editor, GysoTreeView treeView){
        this.datas = datas;
        this.editor = editor;
        this.parent = parent;
        isChange = false;
        isDelete = false;
        this.treeView = treeView;
    }
    @Override
    public int getViewType(NodeModel<Model> model) {
        return model.getValue().getModelType();
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    @Override
    public int getLayoutId(int viewType) {
        switch(viewType)
        {
            case 0: return R.layout.treeview_card;
            case 1: return R.layout.treeview_card;
            case 2: return R.layout.treeview_card;
            case 3: return R.layout.treeview_card;
        }
        return 0;
    }

    public boolean isChange() {
        return isChange;
    }

    @Override
    public void onBindViewHolder(@NonNull TreeViewHolder<Model> holder) {
        View view = holder.getView();
        Model model = holder.getNode().getValue();
        ImageView cardImage = (ImageView)view.findViewById(R.id.card_image);
        cardImage.setOnClickListener(v->{
            model.setShowDetail(!model.isShowDetail());
            notifyItemViewChange(holder.getNode());
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDelete){
                    if(holder.getNode().getValue() == datas){
                        return;
                    }
                    try {
                        ModelGroup modelGroup = (ModelGroup) holder.getNode().getParentNode().getValue();
                        modelGroup.removeModel(holder.getNode().getValue());
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    editor.removeNode(holder.getNode());
                    isChange = true;
                }
            }
        });
        view.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_HOVER_ENTER:
                        treeView.requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        treeView.requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
        TextView cardName = (TextView)view.findViewById(R.id.card_name);
        LinearLayout detailLayout = (LinearLayout) view.findViewById(R.id.model_detail);
        LayoutInflater inflater = (LayoutInflater)parent.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
        switch(model.getModelType()){
            case 0: { // click model
                cardImage.setImageResource(R.drawable.click);
                cardName.setText(String.valueOf(model.getDelay()));
                break;
            }
            case 1: {
                cardImage.setImageResource(R.drawable.scroll);
                cardName.setText(String.valueOf(model.getDelay()));
                break;
            }
            case 2: { // ModelGroup
                cardImage.setImageResource(R.drawable.group);
                cardName.setText(String.valueOf(model.getDelay()));
                break;
            }
            case 3: {
                cardImage.setImageResource(R.drawable.delay);
                cardName.setText(String.valueOf(model.getDelay()));
            }
        }

        if(model.isShowDetail()){
            switch(model.getModelType()){
                case 0:{
                    ClickModel clickModel = (ClickModel) model;
                    switch (clickModel.getMode()){
                        case ClickModel.CLICK_MODE_WIDGET:
                            detailLayout.addView(inflater.inflate(R.layout.click_model_widget, null));
                            break;
                        case ClickModel.CLICK_MODE_POSITION:
                            detailLayout.addView(inflater.inflate(R.layout.click_model_coord, null));
                            break;
                    }
                    RadioGroup triggerGroup = view.findViewById(R.id.trigger_group);
                    switch(clickModel.getMode()){
                        case ClickModel.CLICK_MODE_WIDGET:{

                            TextView widgetInfo = (TextView)view.findViewById(R.id.widget_info);
                            widgetInfo.setText(clickModel.getWidgetDescription().toString());
                            setWidgetPicker(view, holder.getNode(), false);
                            triggerGroup.check(R.id.radio0);
                            break;
                        }
                        case ClickModel.CLICK_MODE_POSITION:{
                            TextView x_edit = (TextView) view.findViewById(R.id.x_edit);
                            x_edit.setText(String.valueOf(clickModel.getX()));
                            TextView y_edit = (TextView) view.findViewById(R.id.y_edit);
                            y_edit.setText(String.valueOf(clickModel.getY()));
                            setWidgetPicker(view, holder.getNode(), false);
                            triggerGroup.check(R.id.radio1);
                            break;
                        }
                    }

                    triggerGroup.setOnCheckedChangeListener((group, checkedId) -> {
                        switch(checkedId)
                        {
                            case R.id.radio0: clickModel.setMode(0);break;
                            case R.id.radio1: clickModel.setMode(1);break;
                        }
                        TreeModelAdapter.this.notifyItemViewChange(holder.getNode());
                        isChange = true;
                    });

                    EditText repeat_edit = (EditText) view.findViewById(R.id.click_model_repeat);
                    repeat_edit.setText(String.valueOf(clickModel.getRepeatTimes()));
                    repeat_edit.addTextChangedListener(new NumTextChangeListener(repeat_edit) {
                        @Override
                        public void onNumInput(int num) {
                            clickModel.setRepeatTimes(num);
                            isChange = true;
                        }
                    });

                    EditText delay_edit = (EditText) view.findViewById(R.id.model_delay);
                    delay_edit.setText(String.valueOf(clickModel.getDelay()));
                    delay_edit.addTextChangedListener(new NumTextChangeListener(delay_edit) {
                        @Override
                        public void onNumInput(int num) {
                            clickModel.setDelay(num);
                            notifyItemViewChange(holder.getNode());
                            isChange = true;
                        }
                    });
                    break;
                }
                case 1:{
                    ScrollModel scrollModel = (ScrollModel)model;
                    switch(scrollModel.getMode()){
                        case ScrollModel.SCROLL_MODE_TIME:
                            detailLayout.addView(inflater.inflate(R.layout.scroll_model_time, null));
                            break;
                        case ScrollModel.SCROLL_MODE_END:
                            detailLayout.addView(inflater.inflate(R.layout.scroll_model_end, null));
                            break;
                        case ScrollModel.SCROLL_MODE_WIDGET:
                            detailLayout.addView(inflater.inflate(R.layout.scroll_model_widget, null));
                            break;
                    }

                    RadioGroup triggerGroup = (RadioGroup)view.findViewById(R.id.scroll_cond_group);
                    switch(scrollModel.getMode()){
                        case ScrollModel.SCROLL_MODE_TIME:{
                            TextView widgetInfo = (TextView)view.findViewById(R.id.widget_info);
                            widgetInfo.setText(scrollModel.getWidgetDescription().toString());
                            setWidgetPicker(view, holder.getNode(), true);
                            EditText timeInput = (EditText)view.findViewById(R.id.scroll_time_input);
                            timeInput.setText(String.valueOf(scrollModel.getScrollTime()));
                            timeInput.addTextChangedListener(new NumTextChangeListener(timeInput) {
                                @Override
                                public void onNumInput(int num) {
                                    scrollModel.setScrollTime(num);
                                    isChange = true;
                                }
                            });
                            triggerGroup.check(R.id.scroll_time_cond);
                            break;
                        }
                        case ScrollModel.SCROLL_MODE_END:{
                            TextView widgetInfo = (TextView)view.findViewById(R.id.widget_info);
                            widgetInfo.setText(scrollModel.getWidgetDescription().toString());
                            setWidgetPicker(view, holder.getNode(), true);
                            triggerGroup.check(R.id.scroll_end_cond);
                            break;
                        }
                        case ScrollModel.SCROLL_MODE_WIDGET:{
                            TextView widgetInfo = (TextView)view.findViewById(R.id.widget_info);
                            widgetInfo.setText(scrollModel.getWidgetDescription().toString());
                            setWidgetPicker(view, holder.getNode(), true);
                            EditText stopInput = (EditText)view.findViewById(R.id.scroll_widget_stop_text);
                            stopInput.setText(scrollModel.getStopText());
                            stopInput.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {

                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    scrollModel.setStopText(String.valueOf(stopInput.getText()));
                                    isChange = true;
                                }
                            });
                            triggerGroup.check(R.id.scroll_widget_cond);
                            break;
                        }
                    }

                    triggerGroup.setOnCheckedChangeListener((group, checkedId) -> {
                        switch(checkedId)
                        {
                            case R.id.scroll_time_cond: scrollModel.setMode(0);break;
                            case R.id.scroll_end_cond: scrollModel.setMode(1);break;
                            case R.id.scroll_widget_cond: scrollModel.setMode(2);break;
                        }
                        TreeModelAdapter.this.notifyItemViewChange(holder.getNode());
                        isChange = true;
                    });

                    EditText delay_edit = (EditText) view.findViewById(R.id.model_delay);
                    delay_edit.setText(String.valueOf(scrollModel.getDelay()));
                    delay_edit.addTextChangedListener(new NumTextChangeListener(delay_edit) {
                        @Override
                        public void onNumInput(int num) {
                            model.setDelay(num);
                            notifyItemViewChange(holder.getNode());
                            isChange = true;
                        }
                    });
                    break;
                }
                case 2:{
                    detailLayout.addView(inflater.inflate(R.layout.group_model, null));
                    EditText repeat_edit = (EditText) view.findViewById(R.id.model_repeat);
                    repeat_edit.setText(String.valueOf(model.getRepeatTimes()));
                    repeat_edit.addTextChangedListener(new NumTextChangeListener(repeat_edit) {
                        @Override
                        public void onNumInput(int num) {
                            model.setRepeatTimes(num);
                            isChange = true;
                        }
                    });

                    EditText delay_edit = (EditText) view.findViewById(R.id.model_delay);
                    delay_edit.setText(String.valueOf(model.getDelay()));
                    delay_edit.addTextChangedListener(new NumTextChangeListener(delay_edit) {
                        @Override
                        public void onNumInput(int num) {
                            model.setDelay(num);
                            notifyItemViewChange(holder.getNode());
                            isChange = true;
                        }
                    });
                    break;
                }
                case 3:{
                    detailLayout.addView(inflater.inflate(R.layout.delay_model_time, null));
                    RadioGroup triggerGroup = (RadioGroup)view.findViewById(R.id.trigger_group);
                    DelayModel delayModel = (DelayModel) model;
                    switch (delayModel.getMode()){
                        case DelayModel.DELAY_MODE_TIME: triggerGroup.check(R.id.delay_cond_time);break;
                        case DelayModel.DELAY_MODE_WINDOW_CHANGE: triggerGroup.check(R.id.delay_cond_state);break;
                    }
                    triggerGroup.setOnCheckedChangeListener((group, checkedId) -> {
                        switch(checkedId){
                            case R.id.delay_cond_time: delayModel.setMode(DelayModel.DELAY_MODE_TIME);break;
                            case R.id.delay_cond_state: delayModel.setMode(DelayModel.DELAY_MODE_WINDOW_CHANGE);break;
                        }
                        isChange = true;
                    });
                    detailLayout.addView(inflater.inflate(R.layout.group_model, null));
                    EditText repeat_edit = (EditText) view.findViewById(R.id.model_repeat);
                    repeat_edit.setText(String.valueOf(model.getRepeatTimes()));
                    repeat_edit.addTextChangedListener(new NumTextChangeListener(repeat_edit) {
                        @Override
                        public void onNumInput(int num) {
                            model.setRepeatTimes(num);
                            isChange = true;
                        }
                    });

                    EditText delay_edit = (EditText) view.findViewById(R.id.model_delay);
                    delay_edit.setText(String.valueOf(model.getDelay()));
                    delay_edit.addTextChangedListener(new NumTextChangeListener(delay_edit) {
                        @Override
                        public void onNumInput(int num) {
                            model.setDelay(num);
                            notifyItemViewChange(holder.getNode());
                            isChange = true;
                        }
                    });
                }
            }
        }
    }

    private void setWidgetPicker(View view, NodeModel<Model> node, boolean onlyScroll){
        Model model = node.getValue();
        ImageButton widgetPickerButton = (ImageButton) view.findViewById(R.id.widget_picker);
        AccessService service = AccessService.getInstance();
        widgetPickerButton.setOnClickListener(view1->{
            if(service != null){
                try {
                    service.setDescription(new CoordinateDescription(),
                            (WidgetDescription) model.getClass().getMethod("getWidgetDescription").invoke(model));
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                };
                service.showActivityCustomizationDialog(onlyScroll, new AccessService.WindowInfoResultListener() {
                    @Override
                    public void onWidgetResult(WidgetDescription description) {
                        if(description != null) {
                            if (model.needWidgetDescription()) {
                                try {
                                    model.getClass().getMethod("setWidgetDescription", WidgetDescription.class).invoke(model, description);
                                    notifyItemViewChange(node);
                                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        Utils.setTopApp(parent);
                    }

                    @Override
                    public void onCoordResult(CoordinateDescription description) {
                        if (description != null) {
                            try {
                                model.getClass().getMethod("setX", int.class).invoke(model, description.x);
                                model.getClass().getMethod("setY", int.class).invoke(model, description.y);
                                notifyItemViewChange(node);
                                isChange = true;
                            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                e.printStackTrace();
                                Debug.error(e.getMessage(), 0);
                            }
                        }
                    }
                });
            }
            else{
                ToastUtil.ToastShort(parent, parent.getString(R.string.accessiblity_not_open));
            }
        });
    }
}
