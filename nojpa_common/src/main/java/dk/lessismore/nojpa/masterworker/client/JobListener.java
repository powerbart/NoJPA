package dk.lessismore.nojpa.masterworker.client;

import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteResultMessage;

public interface JobListener<O> {

    public String getJobID();

    public void onStatus(JobStatus status);

    public void onProgress(double progress);

    // This method is only called once
    public void onResult(O result);

    public void onRunMethodRemoteResult(RunMethodRemoteResultMessage runMethodRemoteResultMessage);


    // This method is only called once
    public void onException(RuntimeException e);
}
