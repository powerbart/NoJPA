package dk.lessismore.nojpa.masterworker.master;

import dk.lessismore.nojpa.net.link.ServerLink;
import dk.lessismore.nojpa.masterworker.messages.*;
import dk.lessismore.nojpa.masterworker.messages.RegistrationMessage;
import dk.lessismore.nojpa.masterworker.messages.HealthMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;


public class MasterWorkerThread extends Thread {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MasterWorkerThread.class);
    private final ServerLink serverLink;
    private final MasterServer masterServer;

    public MasterWorkerThread(MasterServer masterServer, ServerLink serverLink) {
        this.serverLink = serverLink;
        this.masterServer = masterServer;
    }

    public void run() {
        try{
            while(true) {
                Object clientRequest = serverLink.read();
                log.debug("Got clientRequest " + clientRequest);
                if (! (clientRequest instanceof HealthMessage) &&
                    ! (clientRequest instanceof JobProgressMessage))
                    log.debug("Message recieved from worker '" + clientRequest.getClass().getSimpleName() + "'");
                if(clientRequest instanceof PingMessage) {
                    serverLink.write(new PongMessage());
                } else if(clientRequest instanceof RegistrationMessage) {
                    RegistrationMessage registrationMessage = (RegistrationMessage) clientRequest;
                    masterServer.registerWorker(registrationMessage, serverLink);
                } else if(clientRequest instanceof HealthMessage) {
                    HealthMessage healthMessage = (HealthMessage) clientRequest;
                    masterServer.updateWorkerHealth(healthMessage, serverLink);
                } else if(clientRequest instanceof JobProgressMessage) {
                    JobProgressMessage jobProgressMessage = (JobProgressMessage) clientRequest;
                    masterServer.updateJobProgress(jobProgressMessage);
                } else if(clientRequest instanceof RunMethodRemoteResultMessage) {
                    RunMethodRemoteResultMessage runMethodRemoteResultMessage = (RunMethodRemoteResultMessage) clientRequest;
                    masterServer.setRunMethodRemoteResultMessage(runMethodRemoteResultMessage);
                } else if(clientRequest instanceof JobResultMessage) {
                    JobResultMessage jobResultMessage = (JobResultMessage) clientRequest;
                    masterServer.setResult(jobResultMessage, serverLink);
                } else {
                    log.warn("Don't know message from worker = " + clientRequest);
                }
            }
        } catch (ClosedChannelException e) {
            log.info("Connection closed - stopping listening to worker");
        } catch (IOException e) {
            log.error("IOException - stopping listening to worker", e);
        } finally {
            masterServer.unregisterWorker(serverLink);
        }
    }
}
