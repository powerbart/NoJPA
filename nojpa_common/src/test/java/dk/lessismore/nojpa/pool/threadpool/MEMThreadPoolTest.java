package dk.lessismore.nojpa.pool.threadpool;

import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class MEMThreadPoolTest {



    private static class MyJob extends ThreadPoolJob {

        static int counter = 0;

        public MyJob(int id){
            this.fullPathToStoreFile = "-id("+ id +")-" + counter++;

        }
    }

    public static class MyRealWorker implements ThreadPoolWorker {
        @Override
        public void doWork(ThreadPoolJob threadPoolJob) {
            System.out.println("Doing work");
            try {
                Thread.currentThread().sleep(500);
            } catch (InterruptedException e) {

            }
        }
    }

    public static class MyRandomHangerWorker implements ThreadPoolWorker {


        static int counter = 0;


        @Override
        public void doWork(ThreadPoolJob threadPoolJob) {
            System.out.println("Doing work");
            try {
                Thread.currentThread().sleep(counter++ * 1000);
            } catch (InterruptedException e) {
            }
        }
    }




    @Test
    public void testNormal() {
        MEMThreadPool<MyJob> threadPool = new MEMThreadPool<MyJob>(MyRealWorker.class, 3);
        for(int i = 0; i < 10; i++){
            threadPool.addJob(new MyJob(i));
        }

        while(!threadPool.isDone()){
            System.out.println("Waiting ... " + threadPool.size());
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException e) {
            }

        }



    }


    @Test
    public void testRandomHanger() {
        MEMThreadPool<MyJob> threadPool = new MEMThreadPool<MyJob>(MyRandomHangerWorker.class, 3, 3000);
        for(int i = 0; i < 10; i++){
            threadPool.addJob(new MyJob(i));
        }

        while(!threadPool.isDone()){
            System.out.println("Waiting ... " + threadPool.size());
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException e) {
            }

        }



    }



}
