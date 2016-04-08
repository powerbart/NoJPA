package dk.lessismore.nojpa.masterworker.bean.client;

import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteResultMessage;
import dk.lessismore.nojpa.masterworker.executor.SumExecutor;


public class Client {

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

        //JobHandle<String> jobHandle = MasterService.runJob(ToUpperExecutor.class, "WeAreNowUsingTheHandler");
        JobHandle<String> jobHandle = MasterService.runJob(SumExecutor.class, 20000000l);
        jobHandle.addJobListener(jobListener);
        for(int i = 0; i < 100; i++){
            System.out.println("--------------------------- START");
            Object data = jobHandle.getResult();
            System.out.println("data = " + data);
            System.out.println("--------------------------- START");
        }

        System.out.println("jobHandle.getProgress() = " + jobHandle.getProgress());
        System.out.println("jobHandle.getStatus() = " + jobHandle.getStatus());
        System.out.println("jobHandle.getResult() = " + jobHandle.getResult());
        System.out.println("jobHandle.getProgress() = " + jobHandle.getProgress());
        System.out.println("jobHandle.getStatus() = " + jobHandle.getStatus());

        System.out.println("Done");
        jobHandle.close();
    }
}
