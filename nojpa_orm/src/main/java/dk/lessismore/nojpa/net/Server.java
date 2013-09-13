package dk.lessismore.nojpa.net;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.*;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 08-04-11
 * Time: 15:28
 * To change this template use File | Settings | File Templates.
 */
public class Server  extends Thread {

    private Class serverThreadClass;
    private Object initParams;
    private int port;
    private ServerSocket serverSocket = null;
    private boolean running = true;
    private String bindAddress = null;


    final private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Server.class);

    public Server(Class serverThreadClass, Object initParams, String bindAddress, int port) {
        this.bindAddress = bindAddress;
        this.serverThreadClass = serverThreadClass;
        this.initParams = initParams;
        this.port = port;
    }

    public Server(Class serverThreadClass, Object initParams, int port) {
        this.serverThreadClass = serverThreadClass;
        this.initParams = initParams;
        this.port = port;
    }

    public void run() {
        try {
            if(bindAddress == null){
                serverSocket = new ServerSocket(port);
            } else {
                serverSocket = new ServerSocket();
                serverSocket.bind(new InetSocketAddress(InetAddress.getByName(bindAddress), port));
            }
            while (running) {
                Socket client = serverSocket.accept();
                log.debug("Accecpt connection from " + client.getInetAddress() + " / " + client.getRemoteSocketAddress());
                client.setKeepAlive(false);
                client.setSoTimeout(1000 * 30);
                client.setTcpNoDelay(true);
                Constructor constructor = serverThreadClass.getConstructor(initParams != null ? new Class[]{initParams.getClass(), client.getClass()} : new Class[]{client.getClass()});
                log.debug("constructor = " + constructor);
                Thread thread = (Thread) constructor.newInstance(initParams != null ? new Object[]{initParams, client} : new Object[]{client});
                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY + 1);

                log.debug("thread = " + thread);
                thread.start();
            }
        } catch (BindException e) {
            String s = "Error when binding on IP("+ bindAddress +"), port("+ port +") ";
            log.error(s + e.toString(), e);
            log.fatal(s + e.toString(), e);
            e.printStackTrace();
            System.out.print(s);
            System.err.print(s);

            System.exit(1);
        } catch (Throwable e) {
            log.error("Some exception in Server - bindAddress("+ bindAddress +") serverThreadClass("+ serverThreadClass.getSimpleName() +") port("+ port +")... " + e.toString(), e);
        }
    }


    public void stopServer() throws IOException {
        running = false;
        serverSocket.close();
    }


}

