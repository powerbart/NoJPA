package dk.lessismore.nojpa.masterworker.client;

import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.executor.SumExecutor;
import dk.lessismore.nojpa.masterworker.executor.ToUpperExecutor;
import dk.lessismore.nojpa.masterworker.master.MasterServer;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteResultMessage;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by seb on 30/08/2017.
 */
public class ToUpperCaseClient {


    private static final Logger log = LoggerFactory.getLogger(ToUpperCaseClient.class);



    public static void main(String[] args) throws Exception {

        log.debug("debugg");
        log.warn("warnnn");
        log.error("errr");


        JobListener<String> jobListener = null;
//        new JobListener<String>() {
//            public void onStatus(JobStatus status) {
//                System.out.println("onStatus2: "+status);
//            }
//
//            public void onProgress(double progress) {
//                System.out.println("onProgress2: "+progress);
//            }
//
//            public void onResult(String result) {
//                System.out.println("onResult2: "+result);
//            }
//
//            public void onRunMethodRemoteResult(RunMethodRemoteResultMessage runMethodRemoteResultMessage) {
//
//            }
//
//            public void onException(RuntimeException e) {
//                System.out.println("onException: "+e.getMessage());
//            }
//        };

        StringBuilder b = new StringBuilder();
        for(int i = 0; i < 1000; i++){
            b.append("0123456789a"+ i + "::");
        }



        JobHandle<String> jobHandle = null; //TODO: MasterService.runJob(ToUpperExecutor.class, b.toString());
        jobHandle.addJobListener(jobListener);
        System.out.println("jobHandle.getProgress() = " + jobHandle.getProgress());
        System.out.println("jobHandle.getStatus() = " + jobHandle.getStatus());
        //Thread.sleep(10 * 1000);
        System.out.println("Sending KILL - START");
//        jobHandle.kill();
//        System.out.println("Sending KILL - END");
//        Thread.sleep(240 * 1000);

        System.out.println(" ----- GET RESULT ----- ");
        System.out.println("jobHandle.getResult()="+ jobHandle.getResult());
        System.out.println("------ DONE ------");
    }

}
