package dk.lessismore.nojpa.masterworker;

import dk.lessismore.nojpa.masterworker.bean.RandomPrinterBean;
import dk.lessismore.nojpa.masterworker.bean.RemoteBeanInterface;
import dk.lessismore.nojpa.masterworker.bean.worker.RandomPrinterBeanImpl;
import dk.lessismore.nojpa.masterworker.executor.Executor;
import dk.lessismore.nojpa.masterworker.worker.FunWorker;
import dk.lessismore.nojpa.masterworker.worker.Worker;
import dk.lessismore.nojpa.masterworker.executor.ToUpperExecutor;
import dk.lessismore.nojpa.masterworker.executor.SumExecutor;
import dk.lessismore.nojpa.masterworker.bean.worker.BeanExecutor;
import dk.lessismore.nojpa.utils.Pair;
import org.junit.Ignore;

@Ignore
public class TestWorker extends Worker {

    public TestWorker() {
        super(RandomPrinterBean.class, new RandomPrinterBeanImpl(), FunWorker.class, ToUpperExecutor.class, SumExecutor.class);
    }

    public static void main(String[] args) throws Exception {
        Worker worker = new TestWorker();
        worker.run();
    }

}
