package dk.lessismore.reusable_v4.masterworker.master;

import dk.lessismore.reusable_v4.net.link.ServerLink;
import dk.lessismore.reusable_v4.masterworker.messages.*;
import dk.lessismore.reusable_v4.masterworker.messages.observer.ObserverRegistrationMessage;

import java.nio.channels.ClosedChannelException;
import java.io.IOException;

public class MasterObserverThread extends Thread {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MasterObserverThread.class);
    private final ServerLink serverLink;
    private final MasterServer masterServer;

    public MasterObserverThread(MasterServer masterServer, ServerLink serverLink) {
        this.serverLink = serverLink;
        this.masterServer = masterServer;
    }

    public void run() {
        try{
            while(true) {
                Object clientRequest = serverLink.read();
                if (! (clientRequest instanceof HealthMessage) &&
                    ! (clientRequest instanceof JobProgressMessage))
                    log.debug("Message recieved from observer '" + clientRequest.getClass().getSimpleName() + "'");

                if(clientRequest instanceof ObserverRegistrationMessage) {
                    ObserverRegistrationMessage registrationMessage = (ObserverRegistrationMessage) clientRequest;
                    masterServer.registerObserver(serverLink);
                } else {
                    log.warn("Don't know message from observer = " + clientRequest);
                }
            }
        } catch (ClosedChannelException e) {
            log.info("Connection closed - stopping listening to observer");
        } catch (IOException e) {
            log.error("IOException - stopping listening to observer", e);
        } finally {
            masterServer.unregisterWorker(serverLink);
        }
    }


}
