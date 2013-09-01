package dk.lessismore.nojpa.masterworker.master;

import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.messages.JobResultMessage;

public interface ListeningClient {

    public void sendStatus(JobStatus status);

    public void sendProgress(double progress);

    public void sendResult(JobResultMessage result);
}
