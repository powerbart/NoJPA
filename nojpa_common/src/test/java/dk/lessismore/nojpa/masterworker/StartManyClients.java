package dk.lessismore.nojpa.masterworker;

import dk.lessismore.nojpa.guid.GuidFactory;
import dk.lessismore.nojpa.masterworker.client.JobHandle;
import dk.lessismore.nojpa.masterworker.client.JobListener;
import dk.lessismore.nojpa.masterworker.client.MasterService;
import dk.lessismore.nojpa.masterworker.executor.*;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteResultMessage;

import java.util.Date;

public class StartManyClients {

    private static int clientAmount = 1;

//    public static void main(String[] args) throws Exception {
//        final StartManyClients startManyClients = new StartManyClients();
//        startManyClients.startRandomClient(0);
//        Thread.sleep(15 * 1000);
//        startManyClients.startRandomClient(1);
//        Thread.sleep(45 * 1000);
//    }

    public static void main(String[] args) throws Exception {
        for(int t = 0; t < 1; t++){
            final StartManyClients startManyClients = new StartManyClients();
            for (int i = 0; i < clientAmount; i++) {
                final int finalI = i;
                new Thread(new Runnable() {
                    public void run() {
                        System.out.println(finalI + ": Start");
                        startManyClients.startRandomClient(finalI);
                        System.out.println(finalI + ": Done");
                    }
                }).start();
                Thread.sleep(5 * i);
            }
            Thread.sleep(2 * 1000);
        }
        System.out.println("------------- done        ------------");
    }

    private void startRandomClient(int n) {
//        int r = 0; //(int)(Math.random() * 10) % 3;
//        switch (r) {
//            case 0: startSumClient(n);
//            case 1: startToUpperClient(n);
//            case 2: startToLowerClient(n);
//        }
        startFibClient(n);
    }

    private void startFibClient(int n) {
        Thread.currentThread().setName("T-" + n);
        String name = Thread.currentThread().getName();
        System.out.println(name +":1");
        FibInData in = new FibInData();
        in.setArticleID(GuidFactory.getInstance().makeGuid());
        in.setText("Hej");
        System.out.println(name +":2");
        JobHandle<FibOutData> jobHandle = null;
        try {
            System.out.println(name +":3");
            jobHandle = MasterService.runJob(FibExecutor.class, in,10);
            System.out.format("111111111: %d: startFibClient-RESULT: %s\n", n, jobHandle.getResult());
            System.out.println(name +":4");
        } catch (Throwable e){
            System.out.println(name + ": We got an Exception - FIRST ATTEMPT");
            e.printStackTrace();
            System.out.println(name + ": We got an Exception - FIRST ATTEMPT");
            try {
                Thread.sleep(2_000);
            } catch (InterruptedException e1) {

            }
            jobHandle = MasterService.runJob(FibExecutor.class, in,10);
            System.out.format("22222222: %d: startFibClient-RESULT: %s\n", n, jobHandle.getResult());
        }
        System.out.println("----------- RETRY ---------- " + new Date());;
        try {
            System.out.println(name +":3");
            jobHandle = MasterService.runJob(FibExecutor.class, in,10);
            System.out.format("111111111: %d: startFibClient-RESULT: %s\n", n, jobHandle.getResult());
            System.out.println(name +":4");
        } catch (Throwable e){
            System.out.println(name + ": We got an Exception - FIRST ATTEMPT");
            e.printStackTrace();
            System.out.println(name + ": We got an Exception - FIRST ATTEMPT");
            try {
                Thread.sleep(2_000);
            } catch (InterruptedException e1) {

            }
            jobHandle = MasterService.runJob(FibExecutor.class, in,10);
            System.out.format("22222222: %d: startFibClient-RESULT: %s\n", n, jobHandle.getResult());
        }

        System.out.println(name +":5");
        System.out.println(name +":6");
        System.out.println("Start closing " + n);
        jobHandle.close();
        System.out.println(name +":7");
        System.out.println("End closing " + n);
    }

    private void startSumClient(int n) {
        Thread.currentThread().setName("T-" + n);
        String name = Thread.currentThread().getName();
        try {
            System.out.println(name +":1");
            JobHandle<String> jobHandle = MasterService.runJob(SumExecutor.class, 1000l, 10);
            System.out.println(name +":2");
//        jobHandle.setJobListener(new VerboseListener(n));
            System.out.format("%d: startSumClient-RESULT: %s\n", n, jobHandle.getResult());
            System.out.println(name +":3");
            System.out.println("Start closing " + n);
            jobHandle.close();
            System.out.println(name +":4");
            System.out.println("End closing " + n);
        } catch (Exception e){
            System.out.println(name + ": We got an Exception *****************************");
            e.printStackTrace();
        }
    }

    private void startToUpperClient(int n) {
        JobHandle<String> jobHandle = MasterService.runJob(ToUpperExecutor.class, "LilLe PeTer", 60);
//        jobHandle.addJobListener(new VerboseListener(n));
        System.out.format("%d: startToUpperClient-RESULT: %s\n", n, jobHandle.getResult());
        jobHandle.close();
    }

    private void startToLowerClient(int n) {
        JobHandle<String> jobHandle = MasterService.runJob(ToLowerExecutor.class, "StoRe Claus", 60);
//        jobHandle.addJobListener(new VerboseListener(n));
        System.out.format("%d: startToLowerClient-RESULT: %s\n", n, jobHandle.getResult());
        jobHandle.close();
    }

//    class VerboseListener implements JobListener<String> {
//
//        private final int number;
//
//        public VerboseListener(int number) {
//            this.number = number;
//        }
//
//        public void onStatus(JobStatus status) {
//            System.out.format("%d: onStatus: %s\n", number, status);
//        }
//
//        public void onProgress(double progress) {
//            System.out.format("%d: onProgress: %f\n", number, progress);
//        }
//
//        public void onResult(String result) {
//            System.out.format("%d: onResult: %s\n", number, result);
//        }
//
//        public void onRunMethodRemoteResult(RunMethodRemoteResultMessage runMethodRemoteResultMessage) {
//            //To change body of implemented methods use File | Settings | File Templates.
//        }
//
//        public void onException(RuntimeException e) {
//            System.out.format("%d: onException: %s\n", number, e.getMessage());
//        }
//
//
//    }

}
