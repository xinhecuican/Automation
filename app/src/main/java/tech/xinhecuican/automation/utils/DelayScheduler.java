package tech.xinhecuican.automation.utils;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

 public class DelayScheduler extends ScheduledThreadPoolExecutor {

    private static final AtomicLong sequencer = new AtomicLong();
    private LongPack delay = new LongPack();
    private Lock delayLock = new ReentrantLock();

    public DelayScheduler(int corePoolSize) {
        super(corePoolSize);
    }

    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> c, RunnableScheduledFuture<V> task){
        return new MFutureTask<V>(task, delayLock, delay);
    }

    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable r, RunnableScheduledFuture<V> task) {
        return new MFutureTask<V>(task, delayLock, delay);
    }

    public void resetDelay(){
        delayLock.lock();
        try{
            delay.value = 0;
        }
        finally{
            delayLock.unlock();
        }
    }



    class LongPack{
        public long value = 0;
    }

    private class MFutureTask<V> implements RunnableScheduledFuture<V>{

        private RunnableScheduledFuture<V> decorateTask;
        private Lock delayLock;
        private LongPack delay;

        MFutureTask(RunnableScheduledFuture<V> future, Lock delayLock, LongPack delay){
            this.decorateTask = future;
            this.delayLock = delayLock;
            this.delay = delay;
        }

        @Override
        public boolean isPeriodic() {
            return false;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return decorateTask.getDelay(unit) + unit.convert(delay.value, NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return decorateTask.compareTo(o);
        }

        @Override
        public void run() {
            long beginNano = System.nanoTime();
            decorateTask.run();
            long endNano = System.nanoTime();
            long delta = endNano - beginNano;
            this.delayLock.lock();
            try{
                delay.value += delta;
            }
            finally {
                delayLock.unlock();
            }

        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return decorateTask.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return decorateTask.isCancelled();
        }

        @Override
        public boolean isDone() {
            return decorateTask.isDone();
        }

        @Override
        public V get() throws ExecutionException, InterruptedException {
            return decorateTask.get();
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
            return decorateTask.get(timeout, unit);
        }
    }
}