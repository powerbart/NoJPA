package dk.lessismore.nojpa.masterworker.master;

import dk.lessismore.nojpa.masterworker.messages.JobListenMessage;
import dk.lessismore.nojpa.masterworker.messages.JobMessage;
import dk.lessismore.nojpa.masterworker.messages.RestartAllWorkersMessage;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteBeanMessage;
import dk.lessismore.nojpa.net.link.ServerLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;


public class MasterClientThread extends Thread {

    private static final Logger log = LoggerFactory.getLogger(MasterClientThread.class);
    private final ServerLink serverLink;
    private final MasterServer masterServer;

    public MasterClientThread(MasterServer masterServer, ServerLink serverLink) {
        this.serverLink = serverLink;
        this.masterServer = masterServer;
    }

    public void run() {
        try{
            while(true) {
                Object clientRequest = serverLink.read();
                log.debug("Message recieved from client '" + clientRequest.getClass().getSimpleName() + "'");

//                if(clientRequest instanceof NewRemoteBeanMessage) {
//                    NewRemoteBeanMessage newRemoteBeanMessage = (NewRemoteBeanMessage) clientRequest;
//                    masterServer.newRemoteBean(newRemoteBeanMessage);
                if(clientRequest instanceof JobMessage) {
                    JobMessage jobMessage = (JobMessage) clientRequest;
                    masterServer.queueJob(jobMessage);
                } else if(clientRequest instanceof JobListenMessage) {
                    JobListenMessage jobListenMessage = (JobListenMessage) clientRequest;
                    masterServer.startListen(jobListenMessage.getJobID(), serverLink);
                } else if(clientRequest instanceof RestartAllWorkersMessage) {
                    masterServer.restartAllWorkers();
                } else if(clientRequest instanceof RunMethodRemoteBeanMessage) {
                    RunMethodRemoteBeanMessage runMethodRemoteBeanMessage = (RunMethodRemoteBeanMessage) clientRequest;
                    masterServer.runMethodRemote(runMethodRemoteBeanMessage, serverLink);
                } else {
                    System.out.println("Don't know .... clientRequest = " + clientRequest);
                    log.warn("Don't know clientRequest = " + clientRequest);
                }
            }
        } catch (ClosedChannelException e) {
            log.info("Connection closed - stopping listening to client");
        } catch (IOException e) {
            log.error("IOException - stopping listening to client", e);
        } finally {
            masterServer.stopListen(serverLink);
        }
    }
}
