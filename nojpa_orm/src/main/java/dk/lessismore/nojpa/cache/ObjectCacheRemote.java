package dk.lessismore.nojpa.cache;

import dk.lessismore.nojpa.cache.remotecache.ObjectCacheRemotePostThread;
import dk.lessismore.nojpa.cache.remotecache.ObjectCacheRemoteServerThread;
import dk.lessismore.nojpa.net.Server;
import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.resources.PropertyResources;
import dk.lessismore.nojpa.resources.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 */
public class ObjectCacheRemote  {

    private static final Logger log = LoggerFactory.getLogger(ObjectCacheRemote.class);

    public static String clusterFilenameForTest = null;

    private static boolean run = true;

    public static boolean shouldRun() {
        return run;
    }

    public void startUp() {
        try {
            run = true;
            initCluster();
        } catch (Throwable e) {
            log.error("Some error when initCluster() " + e, e);
            e.printStackTrace();
        }
    }

    public void closeDown() {
        try {
            run = false;
            if (server != null) {
                server.stopServer();
            }
            ObjectCacheRemoteServerThread.closeAll();
            for (int i = 0; postThreads != null && i < postThreads.length; i++) {
                postThreads[i].close();
            }
        } catch (Exception e) {
            log.error("Some error when closing ObjectCacheRemoteServer " + e, e);
            e.printStackTrace();
        }
    }

    public static void takeLock(String lockID) {
        for (int i = 0; postThreads != null && i < postThreads.length; i++) {
            log.debug("takeLock(" + i + "/" + postThreads.length + ")->" + postThreads[i]);
            postThreads[i].takeLock(lockID);
        }
    }

    public static void releaseLock(String lockID) {
        for (int i = 0; postThreads != null && i < postThreads.length; i++) {
            log.debug("releaseLock(" + i + "/" + postThreads.length + ")->" + postThreads[i]);
            postThreads[i].releaseLock(lockID);
        }
    }

    public static void sendMessage(String message) {
        for (int i = 0; postThreads != null && i < postThreads.length; i++) {
            log.debug("sendMessage(" + i + "/" + postThreads.length + ")->" + postThreads[i]);
            postThreads[i].sendMessage(message);
        }
    }

    static public class RemoteHost {
        public final String host;
        public final int port;

        public RemoteHost(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String toString() {
            return host + ":" + port;
        }

    }

    static RemoteHost[] hosts = null;
    static int port = 6666;
    static ObjectCacheRemotePostThread[] postThreads = null;
    static Server server = null;


    static void initCluster() {
        log.debug("Checking if we should start ... ");
        PropertyResources resources = PropertyService.getInstance().getPropertyResources(clusterFilenameForTest != null ? clusterFilenameForTest : "cluster");
        log.debug("Checking if we should start ... DONE ... Should we start? : ("+ (resources != null) +")");
        if (resources != null) {
            String portStr = resources.getString("port");
            String bindAddressStr = resources.getString("bindAddress");
            if (portStr != null && portStr.length() > 1) {
                port = Integer.parseInt(portStr);
            }
            List<String> hostList = resources.getList("remoteHosts");
            if (hostList != null && !hostList.isEmpty()) {
                hosts = new RemoteHost[hostList.size()];
                postThreads = new ObjectCacheRemotePostThread[hostList.size()];
                for (int i = 0; i < hostList.size(); i++) {
                    String host = hostList.get(i);
                    if (host.contains(":")) {
                        log.debug("Will now connect to remote host: " + host);
                        hosts[i] = new RemoteHost(host.substring(0, host.indexOf(":")), Integer.parseInt(host.substring(host.indexOf(":") + 1)));
                    } else {
                        hosts[i] = new RemoteHost(host, 6666);
                    }
                    postThreads[i] = new ObjectCacheRemotePostThread(hosts[i]);
                    postThreads[i].start();
                }
                log.debug("STARTING SERVER: ... bindAddressStr(" + bindAddressStr + "):port(" + port + ")");
                if (bindAddressStr != null && bindAddressStr.length() > 2) {
                    server = new Server(ObjectCacheRemoteServerThread.class, null, bindAddressStr, port);
                    server.start();
                } else {
                    server = new Server(ObjectCacheRemoteServerThread.class, null, port);
                    server.start();
                }
                log.debug("Server listener started !!! :-) ");
            }
        }
    }

    public static void gotNewConnection() {
        for (int i = 0; postThreads != null && i < postThreads.length; i++) {
            if (postThreads[i].getState() == Thread.State.WAITING) {
                postThreads[i].errorCounter = 1;
                postThreads[i].interrupt();
            }

        }
    }


    public static void removeFromRemoteCache(ModelObjectInterface modelObject) {
        ModelObject mo = (ModelObject) modelObject;
        if(mo.doRemoteCache()){
            for (int i = 0; postThreads != null && i < postThreads.length; i++) {
                log.debug("removeFromRemoteCache(" + i + "/" + postThreads.length + ")->" + postThreads[i] + " please remove ("+ (modelObject == null ? null : modelObject.getInterface().getSimpleName() + ":" + modelObject)+")");
                postThreads[i].add(modelObject);
            }
        }
    }
}
