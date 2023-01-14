package tech.xinhecuican.automation.model;

import android.accessibilityservice.AccessibilityService;

import java.io.Serializable;

public abstract class Model implements Serializable, Runnable{
    protected AccessibilityService service;
    protected int repeatTimes;
    protected int delay;

    public void setService(AccessibilityService service){
        this.service = service;
    }
    public abstract int getModelType();
    public boolean needWidgetDescription(){
        return false;
    }

    public void setRepeatTimes(int repeatTimes) {
        this.repeatTimes = repeatTimes;
    }

    public int getRepeatTimes() {
        return repeatTimes;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
