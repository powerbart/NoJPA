package dk.lessismore.nojpa.pool.threadpool;

/**
 * User: bart
 * Date: 22-04-2007
 * Time: 23:22:55
 */
public interface ThreadPoolWorker {
    void doWork(ThreadPoolJob job);
}
