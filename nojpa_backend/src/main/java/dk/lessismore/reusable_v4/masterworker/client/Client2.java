package dk.lessismore.reusable_v4.masterworker.client;

import dk.lessismore.reusable_v4.masterworker.executor.ToUpperExecutor;
import dk.lessismore.reusable_v4.masterworker.JobStatus;
import dk.lessismore.reusable_v4.masterworker.messages.RunMethodRemoteResultMessage;


public class Client2 {

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
                System.out.println("onRunMethodRemoteResult: "+runMethodRemoteResultMessage);
            }

            public void onException(RuntimeException e) {
                System.out.println("onException: "+e.getMessage());
            }
        };

        JobHandle<String> jobHandle = MasterService.restoreJobHandle(ToUpperExecutor.class, "abecsasSAS");
        jobHandle.addJobListener(jobListener);
        System.out.println("jobHandle.getProgress() = " + jobHandle.getProgress());
        System.out.println("jobHandle.getStatus() = " + jobHandle.getStatus());
        System.out.println("jobHandle.getResult() = " + jobHandle.getResult());
        System.out.println("jobHandle.getProgress() = " + jobHandle.getProgress());
        System.out.println("jobHandle.getStatus() = " + jobHandle.getStatus());

        System.out.println("Done");
        jobHandle.close();
    }
}