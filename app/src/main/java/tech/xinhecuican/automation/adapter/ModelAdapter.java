package tech.xinhecuican.automation.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import tech.xinhecuican.automation.R;
import tech.xinhecuican.automation.listener.NumTextChangeListener;
import tech.xinhecuican.automation.model.ClickModel;
import tech.xinhecuican.automation.model.Model;

public class ModelAdapter extends RecyclerAdapter<Model>{

    private AppCompatActivity parent;
    private boolean isChange;

    public ModelAdapter(List<Model> datas, AppCompatActivity parent) {
        super(datas);
        this.parent = parent;
        isChange = false;
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
                TextView name = view.findViewById(R.id.click_model_name);
                name.setText(R.string.click);
                TextView info = view.findViewById(R.id.click_model_info);
                info.setText("x: ".concat(String.valueOf(model.getX()))
                        .concat("y: ").concat(String.valueOf(model.getY()))
                        .concat(parent.getString(R.string.click_times)).concat(": ")
                        .concat(String.valueOf(model.getRepeatTimes())));

                TextInputEditText x_edit = (TextInputEditText) view.findViewById(R.id.click_model_x);
                x_edit.addTextChangedListener(new NumTextChangeListener(x_edit) {
                    @Override
                    public void onNumInput(int num) {
                        model.setX(num);
                        isChange = true;
                    }
                });

                TextInputEditText y_edit = (TextInputEditText) view.findViewById(R.id.click_model_y);
                x_edit.addTextChangedListener(new NumTextChangeListener(y_edit) {
                    @Override
                    public void onNumInput(int num) {
                        model.setY(num);
                        isChange = true;
                    }
                });

                TextInputEditText repeat_edit = (TextInputEditText) view.findViewById(R.id.click_model_repeat);
                repeat_edit.addTextChangedListener(new NumTextChangeListener(repeat_edit) {
                    @Override
                    public void onNumInput(int num) {
                        model.setRepeatTimes(num);
                        isChange = true;
                    }
                });
                break;
            }
        }
    }

    public boolean isChange() {
        return isChange;
    }
}
