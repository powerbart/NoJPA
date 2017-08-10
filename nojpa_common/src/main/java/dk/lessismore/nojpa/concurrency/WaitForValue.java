package dk.lessismore.nojpa.concurrency;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WaitForValue<V> {

    private V value;
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private boolean hasValue = false;

    public boolean hasValue() {
        return hasValue;
    }

    public V getValue() {
        try {
            lock.lock();
            while (! hasValue) condition.awaitUninterruptibly();
            V toReturn = value;
            return toReturn;
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
