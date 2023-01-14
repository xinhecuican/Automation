package tech.xinhecuican.automation.adapter;

import android.content.Intent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.List;

import tech.xinhecuican.automation.OperationActivity;
import tech.xinhecuican.automation.R;
import tech.xinhecuican.automation.listener.OperationListenerAdapter;
import tech.xinhecuican.automation.manager.OperationManager;
import tech.xinhecuican.automation.model.Operation;

public class OperationAdapter extends RecyclerAdapter<Operation>  implements RecyclerAdapter.OnItemClickListener {
    private AppCompatActivity parent;
    private OperationManager manager;


    public OperationAdapter(AppCompatActivity parent, List<Operation> datas) {
        super(datas);
        setOnItemClickListener(this);
        this.parent = parent;
        manager = OperationManager.instance();
        manager.addListener(new OperationListenerAdapter(){
            @Override
            public void onOperationDelete(List<Integer> pos) {
                for(int index : pos)
                    notifyItemRemoved(index);
            }
        });
    }

    private class RadioClickListener implements View.OnClickListener{

        private RecyclerAdapter.VH holder;
        private OperationManager manager;
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
        activityName.setText(data.getActivityName());
        TextView time = (TextView) view.findViewById(R.id.operation_time);
        time.setText(String.valueOf(data.getDateEllipse())
                .concat(view.getContext().getString(R.string.day_before)));

        RadioButton radioButton = (RadioButton) view.findViewById(R.id.operation_option_button);
        OperationAdapter.RadioClickListener radioClickListener = new RadioClickListener(holder, manager);
        radioButton.setOnClickListener(radioClickListener);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(parent, OperationActivity.class);
        intent.putExtra("operation", mDatas.get(position));
        intent.putExtra("index", position);
        parent.startActivityForResult(intent, 0);
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }
}
