package dk.lessismore.reusable_v4.masterworker.client;

import dk.lessismore.reusable_v4.masterworker.JobStatus;
import dk.lessismore.reusable_v4.masterworker.messages.RunMethodRemoteResultMessage;

public interface JobListener<O> {

    public void onStatus(JobStatus status);

    public void onProgress(double progress);

    // This method is only called once
    public void onResult(O result);

    public void onRunMethodRemoteResult(RunMethodRemoteResultMessage runMethodRemoteResultMessage);


    // This method is only called once
    public void onException(RuntimeException e);
}
