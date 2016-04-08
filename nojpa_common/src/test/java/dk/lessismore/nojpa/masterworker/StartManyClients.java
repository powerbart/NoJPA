package dk.lessismore.nojpa.masterworker;

import dk.lessismore.nojpa.masterworker.client.JobHandle;
import dk.lessismore.nojpa.masterworker.client.JobListener;
import dk.lessismore.nojpa.masterworker.client.MasterService;
import dk.lessismore.nojpa.masterworker.executor.SumExecutor;
import dk.lessismore.nojpa.masterworker.executor.ToLowerExecutor;
import dk.lessismore.nojpa.masterworker.executor.ToUpperExecutor;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteResultMessage;

public class StartManyClients {

    private static int clientAmount = 50;

//    public static void main(String[] args) throws Exception {
//        final StartManyClients startManyClients = new StartManyClients();
//        startManyClients.startRandomClient(0);
//        Thread.sleep(15 * 1000);
//        startManyClients.startRandomClient(1);
//        Thread.sleep(45 * 1000);
//    }

    public static void main(String[] args) throws Exception {
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
        }
        Thread.sleep(45 * 1000);
//        for(int i = 0; i < clientAmount; i++) {
//            new StartManyClients().startSumClient(10 * i);
//        }
    }

    private void startRandomClient(int n) {
//        int r = 0; //(int)(Math.random() * 10) % 3;
//        switch (r) {
//            case 0: startSumClient(n);
//            case 1: startToUpperClient(n);
//            case 2: startToLowerClient(n);
//        }
        startSumClient(n);
    }

    private void startSumClient(int n) {
        Thread.currentThread().setName("T-" + n);
        JobHandle<String> jobHandle = MasterService.runJob(SumExecutor.class, 100l);
//        jobHandle.addJobListener(new VerboseListener(n));
        System.out.format("%d: RESULT: %s\n", n, jobHandle.getResult());
        System.out.println("Start closing " + n);
        jobHandle.close();
        System.out.println("End closing " + n);
    }

    private void startToUpperClient(int n) {
        JobHandle<String> jobHandle = MasterService.runJob(ToUpperExecutor.class, "LilLe PeTer");
        jobHandle.addJobListener(new VerboseListener(n));
        System.out.format("%d: RESULT: %s\n", n, jobHandle.getResult());
        jobHandle.close();
    }

    private void startToLowerClient(int n) {
        JobHandle<String> jobHandle = MasterService.runJob(ToLowerExecutor.class, "StoRe Claus");
        jobHandle.addJobListener(new VerboseListener(n));
        System.out.format("%d: RESULT: %s\n", n, jobHandle.getResult());
        jobHandle.close();
    }

    class VerboseListener implements JobListener<String> {

        private final int number;

        public VerboseListener(int number) {
            this.number = number;
        }

        public void onStatus(JobStatus status) {
            System.out.format("%d: onStatus: %s\n", number, status);
        }

        public void onProgress(double progress) {
            System.out.format("%d: onProgress: %f\n", number, progress);
        }

        public void onResult(String result) {
            System.out.format("%d: onResult: %s\n", number, result);
        }

        public void onRunMethodRemoteResult(RunMethodRemoteResultMessage runMethodRemoteResultMessage) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void onException(RuntimeException e) {
            System.out.format("%d: onException: %s\n", number, e.getMessage());
        }


    }

}
