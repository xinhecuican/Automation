package tech.xinhecuican.automation.adapter;

import android.graphics.Canvas;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import tech.xinhecuican.automation.utils.Utils;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback{
    /** 通过此变量通知外界发生了排序、删除等操作 */
    private ItemTouchHelperAdapter mAdapter;

    public ItemTouchHelperCallback(ItemTouchHelperAdapter adapter){
        // 注入IDragSwipe
        mAdapter = adapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // 确定拖拽、滑动支持的方向
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    /**
     * 拖拽、交换事件
     */
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        mAdapter.onItemSwapped(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    /**
     * 滑动成功的事件
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        switch (direction) {
            case ItemTouchHelper.END: // START->END 标记完成事件
                mAdapter.onItemDone(viewHolder.getAdapterPosition());
                break;
            case ItemTouchHelper.START: // END->START 删除事件
                mAdapter.onItemDeleted(viewHolder.getAdapterPosition());
                break;
            default:
        }
    }

    /**
     * 拖拽、滑动时如何绘制列表
     * actionState只会为ACTION_STATE_DRAG或者ACTION_STATE_SWIPE
     */
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        switch (actionState) {
            case ItemTouchHelper.ACTION_STATE_DRAG:
                // 拖拽时，如果是isCurrentlyActive，则设置translationZ，否则复位
                viewHolder.itemView.setTranslationZ(Utils.dp2px(isCurrentlyActive ? 4 : 0));
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                break;
            default:
        }
    }

    /**
     * 在onSelectedChanged、onChildDraw、onChildDrawOver操作完成后可以在此进行清楚操作
     */
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
    }
}
