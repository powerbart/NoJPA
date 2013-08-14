package dk.lessismore.reusable_v4.masterworker.messages;

import dk.lessismore.reusable_v4.masterworker.messages.JobRelatedMessage;


public class JobListenMessage extends JobRelatedMessage {

    public JobListenMessage(String jobID) {
        super(jobID);
    }

    public JobListenMessage() {
    }
}
