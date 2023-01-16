package tech.xinhecuican.automation.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.adapter.TreeViewAdapter;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.line.BaseLine;
import com.gyso.treeview.line.DashLine;
import com.gyso.treeview.model.NodeModel;

public abstract class TreeViewBaseAdapter<T>  extends TreeViewAdapter<T> {
    private DashLine dashLine =  new DashLine(Color.parseColor("#F06292"),6);
    @Override
    public TreeViewHolder<T> onCreateViewHolder(@NonNull ViewGroup viewGroup, NodeModel<?> model) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(getLayoutId(getViewType((NodeModel<T>) model)), viewGroup, false);
        return new TreeViewHolder<T>(view, (NodeModel<T>) model);
    }

    public abstract int getViewType(NodeModel<T> model);

    /**
     * 根据viewType返回布局id
     * @param viewType
     * @return 布局id
     */
    public abstract int getLayoutId(int viewType);

    @Override
    public void onBindViewHolder(@NonNull TreeViewHolder<T> holder) {

    }

    @Override
    public BaseLine onDrawLine(DrawInfo drawInfo) {
        return dashLine;
    }

}
