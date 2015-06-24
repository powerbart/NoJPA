package dk.lessismore.nojpa.cache.remotecache;

import dk.lessismore.nojpa.cache.ObjectCacheRemote;
import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.utils.MaxSizeArray;

import java.io.OutputStream;
import java.net.Socket;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 12-04-11
 * Time: 11:13
 * To change this template use File | Settings | File Templates.
 */
//TODO: When start up, check that RemoteServer is not it self
//When starting up, send out a message with some kind of ID + description of server, connections possibility.
// Each server should only resend the message if they haven't see the ID before
public class ObjectCacheRemotePostThread extends Thread {

    final private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ObjectCacheRemotePostThread.class);

    private final MaxSizeArray<String> toPost = new MaxSizeArray<String>(1000);
    private ObjectCacheRemote.RemoteHost host;
    private Socket socket = null;
    private OutputStream outputStream = null;
    public int errorCounter = 1;

    long lastRemoveCounter = 1;
    long lastLockCounter = 1;
    long lastUnlockCounter = 1;


    public ObjectCacheRemotePostThread(ObjectCacheRemote.RemoteHost host) {
        this.host = host;
    }

    public void add(ModelObjectInterface modelObjectInterface) {
        synchronized (toPost) {
            String strToSend = "r:"+ (lastRemoveCounter++) +":" + ((ModelObject) modelObjectInterface).getInterface().getName() + ":" + modelObjectInterface;
            toPost.add(strToSend);
        }
        synchronized (this) {
            notify();
        }
    }

    protected String pull() {
        synchronized (toPost) {
            return toPost.pull();
        }
    }

    protected static void write(String str, OutputStream output) throws Exception {
        output.write((str + "\r\n").getBytes());
        output.flush();
        log.debug("write::DONE::Writing to client " + str);
    }


    public void run() {
        while (ObjectCacheRemote.shouldRun()) {
            try {
                log.debug("Is now making connection to host(" + host.host + ") port(" + host.port + ")");
                socket = new Socket(host.host, host.port);
                outputStream = socket.getOutputStream();
                while (ObjectCacheRemote.shouldRun()) {
                    String s = null;
                    while ((s = pull()) != null) {
                        log.debug("Will write to host(" + host.host + ") port(" + host.port + ") -> " + s);
                        write(s, outputStream);
                        errorCounter = 1;
                    }
                    synchronized (this) {
                        wait(5 * 1000);
                    }
                }
            } catch (Exception e) {
                errorCounter = (1 + errorCounter) * 2;
                log.error("Some error in run() when sending data to host(" + host.host + ") port(" + host.port + ") " + e);
                try {
                    if (outputStream != null) outputStream.close();
                } catch (Exception t) {
                }
                try {
                    if (socket != null) socket.close();
                } catch (Exception t) {
                }
                socket = null;
                outputStream = null;
                try {
                    log.warn("Will now sleep in " + errorCounter + " sec. And the retry...");
                    this.sleep((errorCounter % 120) * 1000); // Will sleep for max 5 mins
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void close() {
        try {
            if (outputStream != null) outputStream.close();
        } catch (Exception t) {
        }
        try {
            if (socket != null) socket.close();
        } catch (Exception t) {
        }
        socket = null;
        outputStream = null;
    }


    public void takeLock(String lockID) {
        synchronized (toPost) {
            String strToSend = "ll:"+ (lastLockCounter++ ) +":" + lockID;
            toPost.add(strToSend);
        }
        synchronized (this) {
            notify();
        }
    }

    public void releaseLock(String lockID) {
        synchronized (toPost) {
            String strToSend = "ul:"+ (lastUnlockCounter++) +":" + lockID;
            toPost.add(strToSend);
        }
        synchronized (this) {
            notify();
        }

    }

    public void sendMessage(String messsage) {
        synchronized (toPost) {
            String strToSend = "ml:" + messsage;
            toPost.add(strToSend);
        }
        synchronized (this) {
            notify();
        }
    }
}
