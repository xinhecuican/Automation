package tech.xinhecuican.automation.adapter;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class RecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerAdapter.VH>{
    protected List<T> mDatas;
    private RecyclerAdapter.OnItemClickListener onItemClickListener;

    public RecyclerAdapter(List<T> datas){
        this.mDatas = datas;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 根据viewType返回布局id
     * @param viewType
     * @return 布局id
     */
    public abstract int getLayoutId(int viewType);

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return VH.get(parent,getLayoutId(viewType));
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        bindData(holder, mDatas.get(position), position);

        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(onItemClickListener != null) {
                    int pos = holder.getLayoutPosition();
                    onItemClickListener.onItemClick(holder.getRootView(), pos);
                }
            }
        });

        holder.getRootView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(onItemClickListener != null) {
                    int pos = holder.getLayoutPosition();
                    onItemClickListener.onItemLongClick(holder.itemView, pos);
                }
                //表示此事件已经消费，不会触发单击事件
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void addItem(T item)
    {
        if(mDatas == null || mDatas.isEmpty())
            Log.d("automationError", "RecyclerAdapter add item");
        mDatas.add(item);
        notifyItemInserted(mDatas.size()-1);
    }

    public void removeItem(int index)
    {
        if(mDatas == null || mDatas.isEmpty())
            Log.d("automationError", "RecyclerAdapter remove item");
        mDatas.remove(index);
        notifyItemRemoved(index);
    }

    /**
     *
     * @param holder ui
     * @param data 数据
     * @param position 位置
     */
    public abstract void bindData(VH holder, T data, int position);

    public static class VH extends RecyclerView.ViewHolder{
        private SparseArray<View> mViews;
        private View mConvertView;

        public VH(View v){
            super(v);
            mConvertView = v;
            mViews = new SparseArray<>();
        }

        public static VH get(ViewGroup parent, int layoutId){
            View convertView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new VH(convertView);
        }

        public <T extends View> T getView(int id){
            View v = mViews.get(id);
            if(v == null){
                v = mConvertView.findViewById(id);
                mViews.put(id, v);
            }
            return (T)v;
        }

        public View getRootView(){return mConvertView;}
    }
}