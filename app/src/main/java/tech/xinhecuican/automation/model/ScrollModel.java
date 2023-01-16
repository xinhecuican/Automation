package tech.xinhecuican.automation.model;

import android.view.accessibility.AccessibilityNodeInfo;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import tech.xinhecuican.automation.utils.Debug;
import tech.xinhecuican.automation.utils.Utils;

public class ScrollModel extends Model implements Serializable {

    private int mode;
    private int scrollTime;
    private WidgetDescription widgetDescription;
    private String stopText;

    public ScrollModel(){
        super();
        mode = 0;
        scrollTime = 0;
        stopText = "";
        repeatTimes = 1;
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

    @Override
    public boolean needWidgetDescription(){
        return true;
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
        Debug.info("scroll run", 0);
        switch(mode){
            case SCROLL_MODE_TIME: {
                AccessibilityNodeInfo node = Utils.findWidgetByDescription(service, widgetDescription);
                if (node != null) {
                    Timer stopTimer = new Timer();
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        }
                    }, 0, 1000);
                    stopTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            timer.cancel();
                        }
                    }, scrollTime * 1000L);
                }
                break;
            }
            case SCROLL_MODE_END: {
                AccessibilityNodeInfo node = Utils.findWidgetByDescription(service, widgetDescription);
                if (node != null) {
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if(!node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))
                                timer.cancel();
                        }
                    }, 0, 1000);
                }
                break;
            }
            case SCROLL_MODE_WIDGET:{
                AccessibilityNodeInfo node = Utils.findWidgetByDescription(service, widgetDescription);
                if (node != null) {
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if(node.getText().equals(stopText))
                                timer.cancel();
                            else
                                node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        }
                    }, 0, 1000);
                }
                break;
            }
        }
    }
}
