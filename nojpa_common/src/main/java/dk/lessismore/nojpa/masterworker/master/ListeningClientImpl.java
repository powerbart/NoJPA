package dk.lessismore.nojpa.masterworker.master;

import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.messages.JobProgressMessage;
import dk.lessismore.nojpa.masterworker.messages.JobResultMessage;
import dk.lessismore.nojpa.masterworker.messages.JobStatusMessage;
import dk.lessismore.nojpa.net.link.ServerLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class ListeningClientImpl implements ListeningClient {

    private static final Logger log = LoggerFactory.getLogger(JobPool.class);

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
                }
            }
        });
        thread.setDaemon(true);
        thread.run();
    }
}
