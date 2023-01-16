package tech.xinhecuican.automation.model;

import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.Serializable;

import tech.xinhecuican.automation.utils.Debug;
import tech.xinhecuican.automation.utils.Utils;

public class ClickModel extends Model implements Serializable {
    private static final long serialVersionUID = -9000991974122377486L;
    private int x;
    private int y;
    private int mode;
    private WidgetDescription widgetDescription;

    public static final int CLICK_MODE_WIDGET = 0;
    public static final int CLICK_MODE_POSITION = 1;

    public ClickModel(){
        super();
        x = 0;
        y = 0;
        repeatTimes = 1;
        delay = 0;
        mode = CLICK_MODE_POSITION;
        widgetDescription = new WidgetDescription();
    }

    public ClickModel(int x, int y, int repeatTimes, int delay)
    {
        this.x = x;
        this.y = y;
        this.repeatTimes = repeatTimes;
        this.delay = delay;
        mode = CLICK_MODE_POSITION;
        widgetDescription = new WidgetDescription();
    }

    @Override
    public int getModelType() {
        return 0;
    }

    @Override
    public boolean needWidgetDescription(){
        return mode == CLICK_MODE_WIDGET;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public WidgetDescription getWidgetDescription() {
        return widgetDescription;
    }

    public void setWidgetDescription(WidgetDescription widgetDescription) {
        this.widgetDescription = widgetDescription;
    }

    @Override
    public void run() {
        Debug.info("click run", 0);
        if(mode == CLICK_MODE_WIDGET){
            long beginTime = System.nanoTime();
            AccessibilityNodeInfo node = Utils.findWidgetByDescription(service, widgetDescription);
            Debug.info("ellipse " + String.valueOf(System.nanoTime() - beginTime), 0);
            if(node != null){
                boolean clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                if(!clicked){
                    Rect rect = new Rect();
                    node.getBoundsInScreen(rect);
                    click(rect.centerX(), rect.centerY());
                }
            }
        }
        else if(mode == CLICK_MODE_POSITION){
            click(this.x, this.y);
        }
    }

    private void click(int x, int y){
        Path path = new Path();
        path.moveTo(x, y);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            GestureDescription.Builder builder = new GestureDescription.Builder().
                    addStroke(new GestureDescription.StrokeDescription(path, 0, 40));
            boolean result = service.dispatchGesture(builder.build(), null, null);
            if(!result)
                Debug.info("click x: " + x + " y: " + y + " error", 0);
        } else {
            Debug.error("SDK can't support click", 0);
        }
    }
}
