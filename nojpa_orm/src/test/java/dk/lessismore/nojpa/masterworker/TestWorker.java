package dk.lessismore.nojpa.masterworker;

import dk.lessismore.nojpa.masterworker.worker.Worker;
import dk.lessismore.nojpa.masterworker.executor.ToUpperExecutor;
import dk.lessismore.nojpa.masterworker.executor.SumExecutor;
import dk.lessismore.nojpa.masterworker.bean.worker.BeanExecutor;
import org.junit.Ignore;

@Ignore
public class TestWorker extends Worker {

    public TestWorker() {
        super(ToUpperExecutor.class, SumExecutor.class, BeanExecutor.class);
    }

    public static void main(String[] args) throws Exception {
        Worker worker = new TestWorker();
        worker.run();
    }

}
