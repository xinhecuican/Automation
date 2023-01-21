package tech.xinhecuican.automation.adapter;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;

import java.util.Comparator;
import java.util.List;

import tech.xinhecuican.automation.OperationActivity;
import tech.xinhecuican.automation.R;
import tech.xinhecuican.automation.manager.OperationManager;
import tech.xinhecuican.automation.model.Operation;
import tech.xinhecuican.automation.model.Storage;
import tech.xinhecuican.automation.utils.Utils;

public class OperationAdapter extends RecyclerAdapter<Operation>  implements
        OperationManager.OperationListener {
    private final AppCompatActivity parent;
    private int selectPostion = -1;
    private MaterialCardView selectView;


    public OperationAdapter(AppCompatActivity parent, List<Operation> datas) {
        super(datas);

        this.parent = parent;
    }

    @Override
    public void onOperationDelete(List<Integer> pos) {
        pos.sort(Comparator.naturalOrder());
        for(int i=0; i<pos.size(); i++)
        {
            notifyItemRemoved(pos.get(i) - i);
        }
    }

    private static class RadioClickListener implements View.OnClickListener{

        private final RecyclerAdapter.VH holder;
        private final OperationManager manager;
        private boolean isChecked;

        RadioClickListener(RecyclerAdapter.VH holder, OperationManager manager)
        {
            this.holder = holder;
            this.manager = manager;
            isChecked = false;
        }

        @Override
        public void onClick(View view) {
            int pos = holder.getLayoutPosition();
            RadioButton button = (RadioButton) view;
            if(!isChecked)
            {
                manager.addDelete(pos);
                button.setChecked(true);
            }
            else
            {
                manager.removeDelete(pos);
                button.setChecked(false);
            }
            isChecked = !isChecked;
        }
    }

    @Override
    public int getLayoutId(int viewType) {
        return R.layout.operation_item;
    }

    @Override
    public void bindData(VH holder, Operation data, int position) {
        CardView view = (CardView) holder.getRootView();
        TextView name = (TextView) view.findViewById(R.id.operation_name);
        name.setText(data.getName());
        TextView operationCount = (TextView) view.findViewById(R.id.operation_number);
        operationCount.setText(String.valueOf(data.getModelCount()));
        TextView activityName = (TextView) view.findViewById(R.id.operation_activity_name);
        activityName.setText(Utils.showActivityName(data.getPackageName(), data.getActivityName()));
        TextView time = (TextView) view.findViewById(R.id.operation_time);
        time.setText(String.valueOf(data.getDateEllipse())
                .concat(view.getContext().getString(R.string.day_before)));

        ImageButton settingButton = (ImageButton) view.findViewById(R.id.operation_setting);
        settingButton.setOnClickListener(v -> {
            Intent intent = new Intent(parent, OperationActivity.class);
            intent.putExtra("index", position);
            parent.startActivityForResult(intent, 0);
        });

        RadioButton radioButton = (RadioButton) view.findViewById(R.id.operation_option_button);
        OperationAdapter.RadioClickListener radioClickListener = new RadioClickListener(holder, OperationManager.instance());
        radioButton.setOnClickListener(radioClickListener);
    }

    @Override
    public void onItemClick(View view, int position) {
        Storage.instance().setChooseActivity(true);
        Storage.instance().setChoosedOperation(mDatas.get(position));
        setSelectPostion(view, position);
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }
    private void setSelectPostion(View view, int pos){
        if(pos == selectPostion)
            return;
        if(selectPostion != -1){
            selectView.setCardBackgroundColor(parent.getColor(com.google.android.material.R.color.cardview_light_background));
        }
        MaterialCardView card = (MaterialCardView) view;
        card.setCardBackgroundColor(parent.getColor(com.google.android.material.R.color.material_dynamic_primary70));
        selectView = card;
        selectPostion = pos;
    }
}
