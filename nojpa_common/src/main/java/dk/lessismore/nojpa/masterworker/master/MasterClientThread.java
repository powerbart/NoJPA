package dk.lessismore.nojpa.masterworker.master;

import dk.lessismore.nojpa.masterworker.messages.*;
import dk.lessismore.nojpa.net.link.ServerLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ClosedChannelException;


public class MasterClientThread implements Runnable {

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
                log.debug("Will wait for client request...");
                Object clientRequest = serverLink.read();
                log.debug("Message recieved from client '" + clientRequest.getClass().getSimpleName() + "'");

//                if(clientRequest instanceof NewRemoteBeanMessage) {
//                    NewRemoteBeanMessage newRemoteBeanMessage = (NewRemoteBeanMessage) clientRequest;
//                    masterServer.newRemoteBean(newRemoteBeanMessage);
                if(clientRequest instanceof JobMessage) {
                    JobMessage jobMessage = (JobMessage) clientRequest;
                    masterServer.queueJob(jobMessage, serverLink);
                } else if(clientRequest instanceof RunMethodRemoteBeanMessage) {
                    RunMethodRemoteBeanMessage runMethodRemoteBeanMessage = (RunMethodRemoteBeanMessage) clientRequest;
                    masterServer.runMethodRemote(runMethodRemoteBeanMessage, serverLink);
                } else if(clientRequest instanceof PongMessage) {
                    //We got a pong ;-)
                } else if(clientRequest instanceof KillMessage) {
                    KillMessage killMessage = (KillMessage) clientRequest;
                    masterServer.kill(killMessage.getJobID());
                } else if(clientRequest instanceof CancelJobMessage) {
                    CancelJobMessage cancelJobMessage = (CancelJobMessage) clientRequest;
                    masterServer.cancelJob(cancelJobMessage.getJobID());
                } else {
                    System.out.println("Don't know .... clientRequest = " + clientRequest);
                    log.warn("Don't know clientRequest = " + clientRequest);
                }
            }
        } catch (ClosedChannelException e) {
            log.info("Connection closed - stopping listening to client");
        } catch (Exception e) {
            log.error("IOException - stopping listening to client", e);
        } finally {
            try{
                log.debug("Saying goodbye to client.... " + serverLink.getLinkID());
                serverLink.close();
            } catch (Exception e){}
            masterServer.clientClosedCancelRunningJobs(serverLink);
        }
    }
}
