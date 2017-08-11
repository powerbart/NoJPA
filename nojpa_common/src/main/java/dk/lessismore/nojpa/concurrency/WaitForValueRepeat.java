package dk.lessismore.nojpa.concurrency;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by seb on 11/08/2017.
 */
public class WaitForValueRepeat<V> extends WaitForValue<V> {

    public V getValue() {
        try {
            lock.lock();
            while (! hasValue) condition.awaitUninterruptibly();
            V toReturn = value;
            value = null;
            hasValue = false;
            return toReturn;
        } finally {
            lock.unlock();
        }
    }
}
