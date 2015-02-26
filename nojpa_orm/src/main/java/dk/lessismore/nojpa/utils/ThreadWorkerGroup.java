package dk.lessismore.nojpa.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */

public class ThreadWorkerGroup<C extends Object> {

    public static interface RunnableWithResult<C extends Object> extends Runnable {
        public C getResult();
    }

    List<Thread> threads = new ArrayList<Thread>();
    List<RunnableWithResult<C>> runResults = new ArrayList<RunnableWithResult<C>>(10);


    public void addAndStart(RunnableWithResult<C> r){
        Thread thread = new Thread(r);
        thread.start();
        threads.add(thread);
        runResults.add(r);
    }

    public void joinAll() throws Exception {
        for(int i = 0; i < threads.size(); i++){
            threads.get(i).join();
        }
    }

    public List<RunnableWithResult<C>> getResults() throws Exception {
        joinAll();
        return runResults;
    }

    private static class MyRunnerMadeForTest implements RunnableWithResult<Long> {

        public long result = 0;

        @Override
        public Long getResult() {
            return result;
        }

        @Override
        public void run() {
            try {
                long l = (long) (Math.random() * 2000);
                Thread.sleep(l);
                result = l;
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


    public static void main(String[] args) throws Exception {
        ThreadWorkerGroup<Long> threadWorkerGroup = new ThreadWorkerGroup<Long>();
        for(int i = 0; i < 10; i++){
            MyRunnerMadeForTest m = new MyRunnerMadeForTest();
            threadWorkerGroup.addAndStart(m);
        }

        System.out.println("Starting join ... ");
        List<RunnableWithResult<Long>> results = threadWorkerGroup.getResults();
        for(int i = 0; i < 10; i++){
            System.out.println("We have result[i]: " + results.get(i).getResult());
        }

    }



}
