package dk.lessismore.reusable_v4.masterworker.master;

import dk.lessismore.reusable_v4.net.link.ServerLink;
import dk.lessismore.reusable_v4.masterworker.JobStatus;
import dk.lessismore.reusable_v4.masterworker.messages.JobResultMessage;
import dk.lessismore.reusable_v4.masterworker.messages.JobStatusMessage;
import dk.lessismore.reusable_v4.masterworker.messages.JobProgressMessage;
import org.apache.log4j.Logger;

import java.io.IOException;


public class ListeningClientImpl implements ListeningClient {

    private static org.apache.log4j.Logger log = Logger.getLogger(JobPool.class);

    private final ServerLink client;
    private final String jobID;
    private final JobPool jobPool;


    public ListeningClientImpl(ServerLink client, String jobID, JobPool jobPool) {
        this.client = client;
        this.jobID = jobID;
        this.jobPool = jobPool;
    }


    public void sendStatus(final JobStatus status) {
        sendCallback(new JobStatusMessage(jobID, status));
    }

    public void sendProgress(double progress) {
        sendCallback(new JobProgressMessage(jobID, progress));
    }

    public void sendResult(JobResultMessage result) {
        sendCallback(result);
    }

    private void sendCallback(final Object message) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    log.debug("Sending callback to client: "+message.getClass().getSimpleName());
                    client.write(message);
                } catch (IOException e) {
                    log.debug("IOException while sending "+message.getClass().getSimpleName()+
                            " to client - removing client");
                    jobPool.removeListener(client);
                }
            }
        });
        thread.setDaemon(true);
        thread.run();
    }
}
