package dk.lessismore.nojpa.masterworker.observer;

import dk.lessismore.nojpa.masterworker.master.MasterProperties;
import dk.lessismore.nojpa.net.link.ClientLink;
import dk.lessismore.nojpa.masterworker.messages.observer.ObserverRegistrationMessage;
import dk.lessismore.nojpa.masterworker.messages.observer.UpdateMessage;
import dk.lessismore.nojpa.properties.PropertiesProxy;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Calendar;

public abstract class AbstractObserver {

    private static final MasterProperties properties = PropertiesProxy.getInstance(MasterProperties.class);
    private static final Logger log = org.apache.log4j.Logger.getLogger(AbstractObserver.class);
    private ObserverCallbackThread callbackThread;
    private final String host;
    private final int port;
    private IOException connectionError;
    private Calendar connectionErrorTime;
    protected ClientLink clientLink; // TODO move back inside

    protected AbstractObserver() {
        host = properties.getHost();
        port = properties.getObserverPort();
        try {
            log.debug("Trying to establish connection to Master on "+host+":"+port);
            clientLink = new ClientLink(host, port);
            clientLink.write(new ObserverRegistrationMessage());
            log.debug("Connection established");

            callbackThread = new ObserverCallbackThread(this, clientLink);
            callbackThread.setDaemon(false);
            callbackThread.start();
        } catch (IOException e) {
            log.debug("Connection error: "+ e.getMessage());
            setConnectionError(e);
        }
    }


    public void stop() {
        callbackThread.halt();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public IOException getConnectionError() {
        return connectionError;
    }

    void setConnectionError(IOException connectionError) {
        this.connectionError = connectionError;
        connectionErrorTime = Calendar.getInstance();
        onConnectionError(connectionError);
    }

    public Calendar getConnectionErrorTime() {
        return connectionErrorTime;
    }

    protected abstract void update(UpdateMessage updateMessage);
    protected void onConnectionError(IOException e) {}
}
