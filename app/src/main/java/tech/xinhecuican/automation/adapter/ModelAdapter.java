package tech.xinhecuican.automation.adapter;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import tech.xinhecuican.automation.AccessService;
import tech.xinhecuican.automation.R;
import tech.xinhecuican.automation.listener.NumTextChangeListener;
import tech.xinhecuican.automation.model.ClickModel;
import tech.xinhecuican.automation.model.CoordinateDescription;
import tech.xinhecuican.automation.model.Model;
import tech.xinhecuican.automation.model.Operation;
import tech.xinhecuican.automation.model.ScrollModel;
import tech.xinhecuican.automation.model.WidgetDescription;
import tech.xinhecuican.automation.utils.ToastUtil;
import tech.xinhecuican.automation.utils.Utils;

public class ModelAdapter extends RecyclerAdapter<Model> implements RecyclerAdapter.OnItemClickListener{

    private AppCompatActivity parent;
    private boolean isChange;
    private AccessService service;
    private Operation operation;

    public ModelAdapter(List<Model> datas, AppCompatActivity parent, Operation operation) {
        super(datas);
        this.parent = parent;
        isChange = false;
        service = AccessService.getInstance();
        setOnItemClickListener(this);
        this.operation = operation;
    }

    @Override
    public int getItemViewType(int position) {
        return mDatas.get(position).getModelType();
    }

    @Override
    public int getLayoutId(int viewType) {
        switch(viewType)
        {
            case 0: return R.layout.click_model_item;
            case 1: return R.layout.scroll_model_item;
        }
        return 0;
    }

    @Override
    public void bindData(VH holder, Model data, int position) {
        switch(data.getModelType())
        {
            case 0: {
                ClickModel model = (ClickModel)data;
                View view = holder.getRootView();
                LinearLayout linearLayout = (LinearLayout) view;
                LayoutInflater inflater = (LayoutInflater)parent.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
                View model_setting;
                if((model_setting = linearLayout.findViewById(R.id.model_setting)) != null)
                    linearLayout.removeView(model_setting);
                if(model.getMode() == ClickModel.CLICK_MODE_WIDGET){
                    linearLayout.addView(inflater.inflate(R.layout.click_model_widget, null));
                }
                else if(model.getMode() == ClickModel.CLICK_MODE_POSITION){
                    linearLayout.addView(inflater.inflate(R.layout.click_model_coord, null));
                }
                TextView name = view.findViewById(R.id.click_model_name);
                name.setText(R.string.click);
                TextView info = view.findViewById(R.id.click_model_info);
                info.setText("x: ".concat(String.valueOf(model.getX()))
                        .concat(" y: ").concat(String.valueOf(model.getY())).concat(" ")
                        .concat(parent.getString(R.string.click_times)).concat(": ")
                        .concat(String.valueOf(model.getRepeatTimes())));

                RadioGroup triggerGroup = (RadioGroup)view.findViewById(R.id.trigger_group);

                switch(model.getMode())
                {
                    case ClickModel.CLICK_MODE_POSITION: {
                        TextView x_edit = (TextView) view.findViewById(R.id.x_edit);
                        x_edit.setText(String.valueOf(model.getX()));
                        TextView y_edit = (TextView) view.findViewById(R.id.y_edit);
                        y_edit.setText(String.valueOf(model.getY()));
                        ImageButton widgetPickerButton = (ImageButton) view.findViewById(R.id.widget_picker);
                        widgetPickerButton.setOnClickListener(view1->{
                            int pos = holder.getLayoutPosition();
                            if(service != null){
                                service.setDescription(pos,
                                        operation.generateCoordDescription(pos), new WidgetDescription());
                                service.showActivityCustomizationDialog();
                            }
                            else{
                                ToastUtil.ToastShort(parent, parent.getString(R.string.accessiblity_not_open));
                            }
                        });
                        triggerGroup.check(R.id.radio1);
                        break;
                    }
                    case ClickModel.CLICK_MODE_WIDGET:
                        TextView widgetInfo = (TextView)view.findViewById(R.id.widget_info);
                        widgetInfo.setText(model.getWidgetDescription().toString());
                        ImageButton widgetPickerButton = (ImageButton) view.findViewById(R.id.widget_picker);
                        widgetPickerButton.setOnClickListener(view1->{
                            int pos = holder.getLayoutPosition();
                            if(service != null){
                                service.setDescription(pos,
                                        new CoordinateDescription(), model.getWidgetDescription());
                                service.showActivityCustomizationDialog();
                            }
                            else{
                                ToastUtil.ToastShort(parent, parent.getString(R.string.accessiblity_not_open));
                            }
                        });
                        triggerGroup.check(R.id.radio0);
                        break;
                }

                triggerGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    switch(checkedId)
                    {
                        case R.id.radio0: model.setMode(0);break;
                        case R.id.radio1: model.setMode(1);break;
                    }
                    ModelAdapter.this.notifyItemChanged(holder.getLayoutPosition());
                    isChange = true;
                });

                TextInputEditText repeat_edit = (TextInputEditText) view.findViewById(R.id.click_model_repeat);
                repeat_edit.setText(String.valueOf(model.getRepeatTimes()));
                repeat_edit.addTextChangedListener(new NumTextChangeListener(repeat_edit) {
                    @Override
                    public void onNumInput(int num) {
                        model.setRepeatTimes(num);
                        isChange = true;
                    }
                });

                TextInputEditText delay_edit = (TextInputEditText) view.findViewById(R.id.click_model_delay);
                delay_edit.setText(String.valueOf(model.getDelay()));
                delay_edit.addTextChangedListener(new NumTextChangeListener(delay_edit) {
                    @Override
                    public void onNumInput(int num) {
                        model.setDelay(num);
                        isChange = true;
                    }
                });
                break;
            }
            case 1:{
                ScrollModel model = (ScrollModel) data;
                View view = holder.getRootView();
                LinearLayout linearLayout = (LinearLayout) view;
                LayoutInflater inflater = (LayoutInflater)parent.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
                View model_setting;
                if((model_setting = linearLayout.findViewById(R.id.model_setting)) != null)
                    linearLayout.removeView(model_setting);
                if(model.getMode() == ScrollModel.SCROLL_MODE_TIME){
                    linearLayout.addView(inflater.inflate(R.layout.scroll_model_time, null));
                }
                else if(model.getMode() == ScrollModel.SCROLL_MODE_END){
                    linearLayout.addView(inflater.inflate(R.layout.scroll_model_end, null));
                }
                else if(model.getMode() == ScrollModel.SCROLL_MODE_WIDGET){
                    linearLayout.addView(inflater.inflate(R.layout.scroll_model_widget, null));
                }

                TextView name = view.findViewById(R.id.scroll_model_name);
                name.setText(R.string.scroll);
                TextView info = view.findViewById(R.id.click_model_info);
                info.setText(parent.getString(R.string.scroll_time).concat(" ")
                        .concat(String.valueOf(model.getScrollTime())));

                RadioGroup triggerGroup = (RadioGroup)view.findViewById(R.id.trigger_group);

                switch(model.getMode()){
                    case ScrollModel.SCROLL_MODE_TIME:{
                        TextInputEditText timeInput = (TextInputEditText)view.findViewById(R.id.scroll_time_input);
                        timeInput.setText(String.valueOf(model.getScrollTime()));
                        timeInput.addTextChangedListener(new NumTextChangeListener(timeInput) {
                            @Override
                            public void onNumInput(int num) {
                                model.setScrollTime(num);
                            }
                        });
                        triggerGroup.check(R.id.scroll_time_cond);
                        break;
                    }
                    case ScrollModel.SCROLL_MODE_END:{
                        triggerGroup.check(R.id.scroll_end_cond);
                        break;
                    }
                    case ScrollModel.SCROLL_MODE_WIDGET:{
                        TextView widgetInfo = (TextView)view.findViewById(R.id.widget_info);
                        widgetInfo.setText(model.getWidgetDescription().toString());
                        ImageButton widgetPickerButton = (ImageButton) view.findViewById(R.id.widget_picker);
                        widgetPickerButton.setOnClickListener(view1->{
                            int pos = holder.getLayoutPosition();
                            if(service != null){
                                service.setDescription(pos,
                                        new CoordinateDescription(), model.getWidgetDescription());
                                service.showActivityCustomizationDialog();
                            }
                            else{
                                ToastUtil.ToastShort(parent, parent.getString(R.string.accessiblity_not_open));
                            }
                        });
                        TextInputEditText stopInput = (TextInputEditText)view.findViewById(R.id.scroll_widget_stop_text);
                        stopInput.setText(model.getStopText());
                        stopInput.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                model.setStopText(String.valueOf(stopInput.getText()));
                            }
                        });
                        triggerGroup.check(R.id.scroll_widget_cond);
                        break;
                    }
                }
            }
        }
    }

    public boolean isChange() {
        return isChange;
    }

    @Override
    public void onItemClick(View view, int position) {
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.model_setting);
        if(layout == null)
            return;
        if(layout.getVisibility() == View.VISIBLE) {
            AnimatorSet animator = (AnimatorSet) AnimatorInflater.loadAnimator(parent, R.animator.hide);
            int height = Utils.calcViewHeight(layout);
            ((ObjectAnimator)animator.getChildAnimations().get(1)).setFloatValues(height, 0);
            animator.setTarget(layout);
            ((ObjectAnimator)animator.getChildAnimations().get(1)).addUpdateListener(valueAnimator -> {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(layout.getLayoutParams());
                params.height = ((Float)valueAnimator.getAnimatedValue()).intValue();
                layout.setLayoutParams(params);
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    layout.setVisibility(View.GONE);
                }
            });
            animator.start();
        }
        else {
            AnimatorSet animator = (AnimatorSet) AnimatorInflater.loadAnimator(parent, R.animator.show);
            int height = Utils.calcViewHeight(layout);
            ((ObjectAnimator)animator.getChildAnimations().get(1)).setFloatValues(0, height);
            animator.setTarget(layout);

            ((ObjectAnimator)animator.getChildAnimations().get(1)).addUpdateListener(valueAnimator -> {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(layout.getLayoutParams());
                params.height = ((Float)valueAnimator.getAnimatedValue()).intValue();
                layout.setLayoutParams(params);
            });
            animator.start();
            layout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }
}
