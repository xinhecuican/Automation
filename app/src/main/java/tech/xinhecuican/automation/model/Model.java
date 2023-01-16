package tech.xinhecuican.automation.model;

import java.io.Serializable;

import tech.xinhecuican.automation.AccessService;

public abstract class Model implements Serializable, Runnable {
    private static final long serialVersionUID = 871710101046765316L;
    protected transient AccessService service = null;
    protected int repeatTimes;
    protected int delay;
    private transient boolean isShowDetail = false;

    Model(){
        delay = 0;
        repeatTimes = 1;
    }

    public void setService(AccessService service){
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

    public boolean isShowDetail() {
        return isShowDetail;
    }

    public void setShowDetail(boolean showDetail) {
        isShowDetail = showDetail;
    }
}
