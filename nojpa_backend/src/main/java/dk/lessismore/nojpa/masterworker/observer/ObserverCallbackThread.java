package dk.lessismore.nojpa.masterworker.observer;

import dk.lessismore.nojpa.net.link.ClientLink;
import dk.lessismore.nojpa.masterworker.messages.observer.UpdateMessage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;


public class ObserverCallbackThread extends Thread {

    private static org.apache.log4j.Logger log = Logger.getLogger(ObserverCallbackThread.class);
    private final ClientLink master;
    private final AbstractObserver observer;

    public ObserverCallbackThread(AbstractObserver observer, ClientLink clientLink) {
        this.master = clientLink;
        this.observer = observer;
    }


    public void run() {
        try{
            while(true) {
                Object message = master.read();
                //log.debug("Message recieved from Master '" + message.getClass().getSimpleName() + "'");

                if(message instanceof UpdateMessage) {
                    UpdateMessage updateMessage = (UpdateMessage) message;
                    observer.update(updateMessage);
                } else {
                    log.error("Don't know message from master= " + message);
                }
            }
        } catch (ClosedChannelException e) {
            log.info("Connection closed - stopping listening for callbacks from master");
            observer.setConnectionError(e);
        } catch (IOException e) {
            log.info("IOException - stopping listening for callbacks from master");
            observer.setConnectionError(e);
        }
    }

    public void halt() {
        master.close();
    }
}