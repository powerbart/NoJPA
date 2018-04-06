package dk.lessismore.nojpa.masterworker.master;

import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.messages.*;
import dk.lessismore.nojpa.net.link.ServerLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MessageSender {

    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

    public static void sendStatusToClient(String jobID, JobStatus jobStatus, ServerLink client, String workerID, FailureHandler failureHandler) {
        log.debug("sendStatusToClient("+ jobID +")");
        JobStatusMessage statusMessage = new JobStatusMessage(jobID, jobStatus, workerID);
        send(statusMessage, client, failureHandler, "sendStatusToClient("+ jobID +") client.getLinkID("+ client.getLinkID() +")");
    }

    public static void sendProgressToClient(String jobID, double progress, ServerLink client, FailureHandler failureHandler) {
        log.debug("sendProgressToClient("+ jobID +")");
        JobProgressMessage progressMessage = new JobProgressMessage(jobID, progress);
        send(progressMessage, client, failureHandler, "sendProgressToClient("+ jobID +") client.getLinkID("+ client.getLinkID() +")");
    }

    public static void sendResultToClient(JobResultMessage result, ServerLink client, FailureHandler failureHandler) {
        log.debug("sendResultToClient("+ result.getJobID() +") with ServerLink("+ client.getLinkID() +")");
        send(result, client, failureHandler, "sendResultToClient("+ result.getJobID() +") client.getLinkID("+ client.getLinkID() +")");
    }

    public static void sendResultToClientAndClose(JobResultMessage result, ServerLink client, FailureHandler failureHandler) {
        log.debug("sendResultToClient("+ result.getJobID() +") with ServerLink("+ client.getLinkID() +")");
        sendAndClose(result, client, failureHandler, "sendResultToClient("+ result.getJobID() +") client.getLinkID("+ client.getLinkID() +")");
    }

    public static void sendRunMethodRemoteResultOfToClient(RunMethodRemoteResultMessage runMethodRemoteResultMessage, ServerLink client, FailureHandler failureHandler) {
        log.debug("sendRunMethodRemoteResultOfToClient: sending " + runMethodRemoteResultMessage);
        send(runMethodRemoteResultMessage, client, failureHandler, "RunMethodRemoteResultOfToClient");
    }
    /**
     * Send message asynchronous.
     * @param message object to send
     * @param client serverLink to recipient
     * @param failureHandler failure callback or null.
     */
    static ThreadPoolExecutor sendExecutor = new ThreadPoolExecutor(40, 5000, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

    public static void send(final Object message, final ServerLink client, final FailureHandler failureHandler, String debugLog) {
        sendExecutor.submit(new Runnable() {
            public void run() {
                try {
//                    log.debug("Writing ("+ debugLog+") - START");
                    client.write(message);
                    if(!(message instanceof PingMessage)){
                        log.debug("Writing ("+ debugLog+") - END");
                    }
                } catch (IOException e) {
                    log.error("Writing ("+ debugLog+") - ERROR: " + e, e);
                    if (failureHandler != null) failureHandler.onFailure(client);
                }
            }});
        synchronized (sendExecutor) {
            if (sendExecutor.getActiveCount() == sendExecutor.getPoolSize()) {
                log.debug("sendExecutor[1.1]: sendExecutor.getActiveCount(" + sendExecutor.getActiveCount() + "), sendExecutor.getPoolSize(" + sendExecutor.getPoolSize() + "), sendExecutor.getQueue().size(" + sendExecutor.getQueue().size() + ")");
                sendExecutor.setCorePoolSize(sendExecutor.getCorePoolSize() + 1);
            }
        }
    }

    public static void sendAndClose(final Object message, final ServerLink client, final FailureHandler failureHandler, String debugLog) {
        sendExecutor.submit(new Runnable() {
            public void run() {
                try {
//                    log.debug("Writing ("+ debugLog+") - START");
                    client.write(message);
                    log.debug("Writing ("+ debugLog+") - END");
//                    client.close();
                } catch (IOException e) {
                    log.error("Writing ("+ debugLog+") - ERROR: " + e, e);
                    if (failureHandler != null) failureHandler.onFailure(client);
                }
            }});
        synchronized (sendExecutor) {
            if (sendExecutor.getActiveCount() == sendExecutor.getPoolSize()) {
                log.debug("sendExecutor[1.1]: sendExecutor.getActiveCount(" + sendExecutor.getActiveCount() + "), sendExecutor.getPoolSize(" + sendExecutor.getPoolSize() + "), sendExecutor.getQueue().size(" + sendExecutor.getQueue().size() + ")");
                sendExecutor.setCorePoolSize(sendExecutor.getCorePoolSize() + 1);
            }
        }
    }

    public static void sendOrTimeout(final Object message, final ServerLink client, final FailureHandler failureHandler) {
        sendExecutor.submit(new Runnable() {
            public void run() {
                try {
                    client.writeWithTimeout(message, 1000);
                } catch (IOException e) {
                    log.debug(e.getClass().getSimpleName() + " while sending "+message.getClass().getSimpleName()+" to client");
                    if (failureHandler != null) failureHandler.onFailure(client);
                }
            }
        });
        synchronized (sendExecutor) {
            if (sendExecutor.getActiveCount() == sendExecutor.getPoolSize()) {
                log.debug("sendExecutor[1.1]: sendExecutor.getActiveCount(" + sendExecutor.getActiveCount() + "), sendExecutor.getPoolSize(" + sendExecutor.getPoolSize() + "), sendExecutor.getQueue().size(" + sendExecutor.getQueue().size() + ")");
                sendExecutor.setCorePoolSize(sendExecutor.getCorePoolSize() + 1);
            }
        }
    }

    public interface FailureHandler {
        void onFailure(ServerLink client);
    }
}
