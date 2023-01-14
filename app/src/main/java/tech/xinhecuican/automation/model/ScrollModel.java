package tech.xinhecuican.automation.model;

import java.io.Serializable;

public class ScrollModel extends Model implements Serializable {

    private int mode;
    private int scrollTime;
    private WidgetDescription widgetDescription;
    private String stopText;

    public ScrollModel(){
        mode = 0;
        scrollTime = 0;
        stopText = "";
        widgetDescription = new WidgetDescription();
    }

    /**
     * 根据滚动时间进行滚动
     */
    public static final int SCROLL_MODE_TIME = 0;

    /**
     * 滚动到底部停止滚动
     */
    public static final int SCROLL_MODE_END = 1;

    /**
     * 根据控件的状态停止滚动
     */
    public static final int SCROLL_MODE_WIDGET = 2;

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public int getScrollTime() {
        return scrollTime;
    }

    public void setScrollTime(int scrollTime) {
        this.scrollTime = scrollTime;
    }

    public void setWidgetDescription(WidgetDescription widgetDescription) {
        this.widgetDescription = widgetDescription;
    }

    public WidgetDescription getWidgetDescription() {
        return widgetDescription;
    }

    public String getStopText() {
        return stopText;
    }

    public void setStopText(String stopText) {
        this.stopText = stopText;
    }

    @Override
    public int getModelType() {
        return 1;
    }

    @Override
    public void run() {
        switch(mode){
            case SCROLL_MODE_TIME:

                break;
        }
    }
}
