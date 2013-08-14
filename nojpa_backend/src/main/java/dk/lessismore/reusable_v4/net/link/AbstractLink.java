package dk.lessismore.reusable_v4.net.link;


import dk.lessismore.reusable_v4.serialization.Serializer;
import dk.lessismore.reusable_v4.serialization.XmlSerializer;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.nio.channels.ClosedChannelException;

public abstract class AbstractLink {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractLink.class);

    protected Socket socket = null;
    protected OutputStream out = null;
    protected InputStream in = null;
    private final LinkedList<Object> receivedObjects = new LinkedList<Object>();
    private final Serializer serializer;
    private static final String SEPARATOR = "<~>";
    private Pinger pinger;


    protected AbstractLink(Serializer serializer) {
        if (serializer == null) {
            this.serializer = new XmlSerializer();
        } else {
            this.serializer = serializer;
        }
    }

    protected AbstractLink() {
        this(null);
    }

    /**
     * Send object to receiver.
     * @param o Object to send.
     * @throws IOException Connection error.
     */
    public synchronized void write(Object o) throws IOException {
        String serializedObject = serializer.serialize(o);
        log.debug("Writing: " + o);
        out.write((serializedObject + SEPARATOR).getBytes());
        out.flush();
    }

    /**
     * Send object to receiver.
     * A WriteTimeoutException is thrown of the message is not sent within the given time.
     * @param o Object to send.
     * @param timeout time in milliseconds
     * @throws WriteTimeoutException Timeout exceeded.
     * @throws IOException Some socket error
     */
    public void writeWithTimeout(Object o, final int timeout) throws IOException {
        final String serializedObject = serializer.serialize(o);
        final Boolean[] writeCompleted = new Boolean[1];
        final IOException[] writeException = new IOException[1];

        final Thread timeoutThread = new Thread(new Runnable() {
            IOException e = null;
            public void run() {
                try {
                    Thread.sleep(timeout);
                    try {
                        out.close();
                    } catch (IOException e1) {
                        log.error("Failed to close socket", e1);
                    }
                } catch (InterruptedException e1) {
                    // Thread interrupted on purpose !
                }
            }
        });

        Thread writeThread = new Thread(new Runnable() {
            public void run() {
                try {
                    out.write((serializedObject + SEPARATOR).getBytes());
                    out.flush();
                    writeCompleted[0] = true;
                    timeoutThread.interrupt();
                } catch (SocketException e) {
                    if (e.getMessage().equalsIgnoreCase("Socket closed")) {
                        writeCompleted[0] = false;
                    } else {
                        writeException[0] = e;
                    }
                } catch (IOException e) {
                    writeException[0] = e;
                }
            }
        });

        timeoutThread.start();
        writeThread.start();
        try {
            timeoutThread.join();
            writeThread.join();
        } catch (InterruptedException e) {
            log.error("Failed to join threads", e);
        }

        if (writeException[0] != null) {
            throw writeException[0];
        } else if ( ! writeCompleted[0]) {
            throw new WriteTimeoutException("Time on "+timeout+"ms exceeded while writing on link");
        }
    }


    /**
     * Blocks until an object are received.
     * @return Received object.
     * @throws ClosedChannelException Connection closed.
     * @throws IOException Connection error.
     */
    public Object read() throws IOException {
        synchronized (receivedObjects) {
            if (! receivedObjects.isEmpty()) {
                Object recivedObject = receivedObjects.getFirst();
                receivedObjects.removeFirst();
                log.debug("Reading: " + recivedObject);
                return recivedObject;
            } else {
                byte[] buffer = new byte[4 * 1024];
                StringBuilder builder = new StringBuilder();
                while (builder.lastIndexOf(SEPARATOR) != builder.length() - SEPARATOR.length()){
                    int byteLength = 0;
                    try {
                        byteLength = in.read(buffer);
                    } catch (SocketException e) {
                        throw new ClosedChannelException();
                    }
                    if (byteLength == -1) {
                        throw new ClosedChannelException();
                    } else {
                        String s = new String(buffer, 0, byteLength);
                        builder.append(s);
                    }
                }
                String s = builder.toString();
                int startIndex = 0;
                while (startIndex != -1) {
                    int endIndex = s.indexOf(SEPARATOR, startIndex);
                    if (endIndex != -1) {
                        String objectStr = s.substring(startIndex, endIndex);
                        Object o = serializer.unserialize(objectStr);
                        if (! (o instanceof Ping)) {
                            receivedObjects.addLast(o);
                        }
                        startIndex = endIndex + SEPARATOR.length();
                    } else {
                        startIndex = endIndex;
                    }
                }
                return read();
            }
        }
    }

    public String getLocalHost() {
        return socket.getLocalAddress().getHostName();
    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }

    public String getLocalHostPort() {
        return "" + getLocalHost() + ":" + getLocalPort();
    }

    public String getOtherHost() {
        return socket.getInetAddress().getHostName();
    }

    public int getOtherPort() {
        return socket.getPort();
    }

    public String getOtherHostPort() {
        return "" + getOtherHost() + ":" + getOtherPort();
    }

    public void close() {
        try {
            stopPinger();
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void startPinger() {
        if (pinger == null) {
            pinger = new Pinger();
            pinger.start();
        } else {
            System.out.println("Pinger all ready running");
        }
    }

    public void stopPinger() {
        if (pinger != null) {
            pinger.stopPinger();
            pinger.interrupt();
            pinger = null;
        }

    }

    private class Pinger extends Thread {
        private boolean run = true;

        public Pinger(){
            this.setPriority(Thread.MIN_PRIORITY);
            this.setDaemon(true);
        }

        public void stopPinger() {
            run = false;
        }

        public void run(){
            log.debug("Pinger is now running");
            while(run){
                try{
//                    log.debug("Sending ping");
                    write(new Ping());
                    try{
                        Thread.sleep(10 * 1000);
                    } catch(InterruptedException e) {
                        log.debug("Sleep interupted - exiting pinger");
                        stopPinger();
                    }
                } catch(IOException e) {
                    log.debug("IOException while sending PING: "+e.getMessage() + " - closing down pinger.");
                    stopPinger();
                }
            }
            log.debug("Pinger is shutting down");
        }
    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName()+" "+socket.toString();
    }
}
