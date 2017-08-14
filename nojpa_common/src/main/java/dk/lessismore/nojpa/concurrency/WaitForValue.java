package dk.lessismore.nojpa.concurrency;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WaitForValue<V> {

    protected V value;
    protected final Lock lock = new ReentrantLock();
    protected final Condition condition = lock.newCondition();
    protected boolean hasValue = false;

    public boolean hasValue() {
        return hasValue;
    }

    public V getValue() {
        if(value != null) return value;
        try {
            lock.lock();
            while (! hasValue) condition.awaitUninterruptibly();
            return value;
        } finally {
            lock.unlock();
        }
    }

    public void setValue(V value) {
        try {
            lock.lock();
            this.value = value;
            hasValue = true;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
