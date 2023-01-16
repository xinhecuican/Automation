package tech.xinhecuican.automation.model;

import java.io.Serializable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import tech.xinhecuican.automation.AccessService;
import tech.xinhecuican.automation.utils.Debug;

public class DelayModel extends Model implements Serializable, AccessService.WindowStateChangeListener {

    private int mode;
    private final transient Lock lock;
    private final transient Condition condition;

    public static final int DELAY_MODE_TIME = 0;
    public static final int DELAY_MODE_WINDOW_CHANGE = 1;

    public DelayModel(){
        super();
        mode = 0;
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }

    @Override
    public int getModelType() {
        return 3;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public void run() {
        Debug.info("delay model run", 0);
        switch (mode){
            case DELAY_MODE_TIME:break;
            case DELAY_MODE_WINDOW_CHANGE:
                try {
                    lock.lock();
                    condition.await();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally{
                    lock.unlock();
                }
                break;
        }
    }

    @Override
    public void onWindowStateChange() {
        try {
            lock.lock();
            condition.signalAll();
        } finally{
            lock.unlock();
        }
    }
}
