package tech.xinhecuican.automation.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import tech.xinhecuican.automation.AccessService;
import tech.xinhecuican.automation.R;

public class ActivityNameAdapter extends RecyclerAdapter<AccessService.ActivityInfo>{
    private int selectPostion;
    private LinearLayout selectView;
    private Map<String, String> nameMap;

    public ActivityNameAdapter(Map<String, String> nameMap, List<AccessService.ActivityInfo> datas) {
        super(datas);
        this.nameMap = nameMap;
        selectPostion = -1;
    }

    @Override
    public int getLayoutId(int viewType) {
        return R.layout.window_state_item;
    }

    @Override
    public void bindData(VH holder, AccessService.ActivityInfo data, int position) {
        TextView packageNameView = (TextView)holder.getRootView().findViewById(R.id.package_name);
        int index = -1;
        if((index = data.packageName.lastIndexOf('.')) != -1){
            packageNameView.setText(nameMap.containsKey(data.packageName) ? nameMap.get(data.packageName) : data.packageName.substring(index+1));
        }
        else{
            packageNameView.setText(data.packageName);
        }
        TextView textView = (TextView)holder.getRootView().findViewById(R.id.activity_name);
        textView.setText(data.activityName);
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
            selectView.setBackgroundColor(Color.parseColor("#00000000"));
        }
        LinearLayout card = (LinearLayout) view;
        card.setBackgroundColor(Color.parseColor("#4BB6E87F"));
        selectView = card;
        selectPostion = pos;
    }
}
