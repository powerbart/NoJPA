package dk.lessismore.nojpa.masterworker;

import dk.lessismore.nojpa.masterworker.executor.Executor;
import dk.lessismore.nojpa.masterworker.executor.SumExecutor;
import dk.lessismore.nojpa.masterworker.executor.ToLowerExecutor;
import dk.lessismore.nojpa.masterworker.executor.ToUpperExecutor;
import dk.lessismore.nojpa.masterworker.worker.Worker;

import java.util.*;

public class StartManyWorkers {

    private static int workerAmount = 20;
    List<? extends Class<? extends Executor>> executorClasses = Arrays.asList(SumExecutor.class, ToUpperExecutor.class, ToLowerExecutor.class);

    public static void main(String[] args) {
        new StartManyWorkers();
    }

    public StartManyWorkers() {
        for (int i = 0; i < workerAmount; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                public void run() {
                    System.out.println(finalI + ": Start");
                    startWorker();
                    System.out.println(finalI + ": Done");
                }
            }).start();

        }
    }

    private void startWorker() {
        new TestWorker().run();
    }

    synchronized private List<? extends Class<? extends Executor>> getSomeExecutorClasses() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Collections.shuffle(executorClasses);
        int n = (int)(Math.random() * executorClasses.size()) +1;
        return new ArrayList(executorClasses.subList(0,n));
    }

    private class TestWorker extends Worker {
        public TestWorker() {
            super(getSomeExecutorClasses());
        }
    }
}
