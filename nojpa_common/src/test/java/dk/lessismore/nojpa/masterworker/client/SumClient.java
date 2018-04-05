package dk.lessismore.nojpa.masterworker.client;

import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteResultMessage;
import dk.lessismore.nojpa.masterworker.executor.SumExecutor;


/**
 * Test client that runs a cpu consuming job and print status, progress and
 * the result to stdout.
 */
public class SumClient {

    public static void main(String[] args) throws Exception {

        JobListener<String> jobListener = new JobListener<String>() {
            public void onStatus(JobStatus status) {
                System.out.println("onStatus: "+status);
            }

            public void onProgress(double progress) {
                System.out.println("onProgress: "+progress);
            }

            public void onResult(String result) {
                System.out.println("onResult: "+result);
            }

            public void onRunMethodRemoteResult(RunMethodRemoteResultMessage runMethodRemoteResultMessage) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void onException(RuntimeException e) {
                System.out.println("onException: "+e.getMessage());
            }
        };

        {
            JobHandle<String> jobHandle = MasterService.runJob(SumExecutor.class, 7L, 60);
            jobHandle.addJobListener(jobListener);
            System.out.println("jobHandle.getProgress() = " + jobHandle.getProgress());
            System.out.println("jobHandle.getStatus() = " + jobHandle.getStatus());
            //Thread.sleep(10 * 1000);
//            System.out.println("Sending KILL - START");
//        jobHandle.kill();
//        System.out.println("Sending KILL - END");
//        Thread.sleep(240 * 1000);

            System.out.println(" ----- GET RESULT ----- ");
            System.out.println(jobHandle.getResult());
            System.out.println("------ DONE ------");
        }
        System.out.println("***************************************************");
        {
            JobHandle<String> jobHandle = MasterService.runJob(SumExecutor.class, 9L, 60);
            jobHandle.addJobListener(jobListener);
            System.out.println("jobHandle.getProgress() = " + jobHandle.getProgress());
            System.out.println("jobHandle.getStatus() = " + jobHandle.getStatus());
            //Thread.sleep(10 * 1000);
//            System.out.println("Sending KILL - START");
//        jobHandle.kill();
//        System.out.println("Sending KILL - END");
//        Thread.sleep(240 * 1000);

            System.out.println(" ----- GET RESULT ----- ");
            System.out.println(jobHandle.getResult());
            System.out.println("------ DONE ------");
        }
    }
}