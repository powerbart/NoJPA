package dk.lessismore.nojpa.pool.threadpool;

import dk.lessismore.nojpa.guid.GuidFactory;
import dk.lessismore.nojpa.reflection.db.DbObjectVisitor;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import java.io.File;

/**
 * Created on 6/7/16.
 */
public class AbstractCountingThreadVisitor<T extends ModelObjectInterface> implements DbObjectVisitor<T> {

    public interface CanProcess<T extends ModelObjectInterface> {
        void process(T t);
    }

    private int counter;
    private MEMThreadPool pool = null;
    int poolMaxSize;
    CanProcess canProcess;
    private static String FOLDER = "/tmp/workerpool";
    private File folder;

    public AbstractCountingThreadVisitor(int countOfThreads, int poolMaxSize, long maxRunningTimeInMillis, CanProcess canProcess) {
        pool = new MEMThreadPool(AbstractCountingThreadPoolWorker.class, countOfThreads, maxRunningTimeInMillis, canProcess);
        this.poolMaxSize = poolMaxSize;
        this.canProcess = canProcess;
        folder = new File(FOLDER);
        if(!folder.exists()) {
            folder.mkdirs();
        }
    }

    @Override
    public void setDone(boolean b) {
    }

    @Override
    public boolean getDone() {
        return false;
    }

    @Override
    public void visit(T t) {
        counter++;
        while(pool.size() > poolMaxSize) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {}
        }
        AbstractCountingThreadPoolJob job = new AbstractCountingThreadPoolJob();
        job.id = GuidFactory.getInstance().makeGuid();
        job.fullPathToStoreFile = FOLDER +"/"+job.id;
        job.t = t;
        pool.addJob(job);
    }

    public int getCounter() {
        return counter;
    }

    static public class AbstractCountingThreadPoolJob<T extends ModelObjectInterface> extends ThreadPoolJob  {
        T t;
    }

    public static class AbstractCountingThreadPoolWorker<T extends ModelObjectInterface> implements ThreadPoolWorker {

        CanProcess canProcess;

        public AbstractCountingThreadPoolWorker(CanProcess canProcess) {
            this.canProcess = canProcess;

        }

        @Override
        public void doWork(ThreadPoolJob job) {
            T t = ((AbstractCountingThreadPoolJob<T>)job).t;
            canProcess.process(t);
        }

    }

}