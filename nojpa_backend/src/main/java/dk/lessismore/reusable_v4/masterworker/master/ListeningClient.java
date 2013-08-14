package dk.lessismore.reusable_v4.masterworker.master;

import dk.lessismore.reusable_v4.masterworker.JobStatus;
import dk.lessismore.reusable_v4.masterworker.messages.JobResultMessage;

public interface ListeningClient {

    public void sendStatus(JobStatus status);

    public void sendProgress(double progress);

    public void sendResult(JobResultMessage result);
}
