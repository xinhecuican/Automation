package tech.xinhecuican.automation.model;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import tech.xinhecuican.automation.AccessService;
import tech.xinhecuican.automation.utils.Debug;
import tech.xinhecuican.automation.utils.Utils;

public class ConditionModel extends Model implements Serializable, AccessService.WindowStateChangeListener {
    private static final long serialVersionUID = -4256965041760720843L;
    private int condition = 0;
    private Model successModel = null;
    private Model failModel = null;
    private int timeout = 0;
    private WidgetDescription widgetDescription = new WidgetDescription();
    protected transient List<ScheduledFuture> successFutures;
    protected transient List<ScheduledFuture> failFutures;
    protected transient Lock lock = new ReentrantLock();
    protected transient Condition lockCondition = lock.newCondition();
    protected transient boolean isSolved;

    public ConditionModel(){
        condition = 0;
        successModel = null;
        failModel = null;
        timeout = 0;
        widgetDescription = new WidgetDescription();
    }

    public static final int COND_WINDOW_STATE = 0;

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }

    public Model getSuccessModel() {
        return successModel;
    }

    public void setSuccessModel(Model successModel) {
        this.successModel = successModel;
    }

    public Model getFailModel() {
        return failModel;
    }

    public void setFailModel(Model failModel) {
        this.failModel = failModel;
    }

    public WidgetDescription getWidgetDescription() {
        return widgetDescription;
    }

    public void setWidgetDescription(WidgetDescription widgetDescription) {
        this.widgetDescription = widgetDescription;
    }

    @Override
    public boolean needWidgetDescription() {
        return true;
    }

    public void exchangeModel(){
        Model tmp = successModel;
        successModel = failModel;
        failModel = tmp;
    }

    public boolean addModel(Model model){
        if(successModel == null){
            successModel = model;
            return true;
        }
        if(failModel == null){
            failModel = model;
            return true;
        }
        return false;
    }

    public void removeModel(Model model){
        if(successModel == model){
            successModel = null;
        }
        if(failModel == model){
            failModel = null;
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    @Override
    public int getModelType() {
        return 4;
    }

    @Override
    public void onRun() {
        Debug.info("condition model run", 0);
        try {
            lock.lockInterruptibly();
            isSolved = false;
            lockCondition.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            lock.unlock();
            if(!isSolved){
                if(successFutures != null){
                    for(ScheduledFuture future : successFutures){
                        future.cancel(true);
                    }
                    successFutures = null;
                }
                service.removeWindowStateChangeListener(this);
            }
        }
    }

    @Override
    public void onWindowStateChange(String activityName) {
        if(activityName.equals(Utils.showActivityName(widgetDescription.packageName, widgetDescription.activityName))){
            if(failFutures != null){
                for(ScheduledFuture future : failFutures){
                    future.cancel(true);
                }
                failFutures = null;
            }
        }
        else{
            if(successFutures != null){
                for(ScheduledFuture future : successFutures){
                    future.cancel(true);
                }
                successFutures = null;
            }
        }
        try{
            lock.lockInterruptibly();
            isSolved = true;
            lockCondition.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }
}
