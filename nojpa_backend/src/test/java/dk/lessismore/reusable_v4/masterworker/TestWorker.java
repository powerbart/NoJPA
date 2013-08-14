package dk.lessismore.reusable_v4.masterworker;

import dk.lessismore.reusable_v4.masterworker.worker.Worker;
import dk.lessismore.reusable_v4.masterworker.executor.ToUpperExecutor;
import dk.lessismore.reusable_v4.masterworker.executor.SumExecutor;
import dk.lessismore.reusable_v4.masterworker.bean.worker.BeanExecutor;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

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
