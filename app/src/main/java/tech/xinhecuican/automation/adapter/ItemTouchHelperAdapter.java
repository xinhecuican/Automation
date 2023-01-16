package tech.xinhecuican.automation.adapter;

public interface ItemTouchHelperAdapter {
    /**
     * 两个Item交换位置
     * @param fromPosition 第一个Item的位置
     * @param toPosition 第二个Item的位置
     */
    void onItemSwapped(int fromPosition, int toPosition);

    /**
     * 删除Item
     * @param position 待删除Item的位置
     */
    void onItemDeleted(int position);

    /**
     * Item标记完成
     * @param position Item的位置
     */
    void onItemDone(int position);
}
