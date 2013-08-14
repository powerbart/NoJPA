package dk.lessismore.reusable_v4.masterworker.master;

import dk.lessismore.reusable_v4.net.link.ServerLink;
import dk.lessismore.reusable_v4.masterworker.messages.JobMessage;
import dk.lessismore.reusable_v4.masterworker.messages.JobListenMessage;
import dk.lessismore.reusable_v4.masterworker.messages.NewRemoteBeanMessage;
import dk.lessismore.reusable_v4.masterworker.messages.RunMethodRemoteBeanMessage;
import org.apache.log4j.Logger;

import java.nio.channels.ClosedChannelException;
import java.io.IOException;


public class MasterClientThread extends Thread {

    private static Logger log = Logger.getLogger(MasterClientThread.class);
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
