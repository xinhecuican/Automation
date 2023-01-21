package tech.xinhecuican.automation.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import tech.xinhecuican.automation.R;

public class ActivityNameAdapter extends RecyclerAdapter<String>{
    private int selectPostion;
    private LinearLayout selectView;

    public ActivityNameAdapter(List<String> datas) {
        super(datas);
        selectPostion = -1;
    }

    @Override
    public int getLayoutId(int viewType) {
        return R.layout.window_state_item;
    }

    @Override
    public void bindData(VH holder, String data, int position) {
        TextView textView = (TextView)holder.getRootView().findViewById(R.id.activity_name);
        textView.setText(data);
    }

    @Override
    public void onItemClick(View view, int position) {
        setSelectPostion(view, position);
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    private void setSelectPostion(View view, int pos){
        if(pos == selectPostion)
            return;
        if(selectPostion != -1){
            selectView.setBackgroundColor(Color.parseColor("#00FFF-FFF"));
        }
        LinearLayout card = (LinearLayout) view;
        card.setBackgroundColor(Color.parseColor("#4BB6E87F"));
        selectView = card;
        selectPostion = pos;
    }
}
