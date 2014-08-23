package dk.lessismore.nojpa.cache.remotecache;

import dk.lessismore.nojpa.cache.GlobalLockService;
import dk.lessismore.nojpa.cache.ObjectCache;
import dk.lessismore.nojpa.cache.ObjectCacheFactory;
import dk.lessismore.nojpa.cache.ObjectCacheRemote;
import dk.lessismore.nojpa.utils.MaxSizeArray;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 08-04-11
 * Time: 15:30
 * To change this template use File | Settings | File Templates.
 */
public class ObjectCacheRemoteServerThread  extends Thread {

    final private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ObjectCacheRemoteServerThread.class);

    static MaxSizeArray<ObjectCacheRemoteServerThread> allCurrentThreads = new MaxSizeArray<ObjectCacheRemoteServerThread>(32);

    public static void closeAll(){
        for(Iterator<ObjectCacheRemoteServerThread> iterator = allCurrentThreads.iterator(); iterator.hasNext(); ){
            iterator.next().close();
        }

    }



    private InputStream input = null;
    private OutputStream output = null;
    private Socket client = null;



    public ObjectCacheRemoteServerThread(Socket client) {
        this.client = client;
        allCurrentThreads.add(this);
    }

//    public static void write(String str, OutputStream output) throws Exception {
//       output.write((str + "\r\n").getBytes());
//       output.flush();
//       //log.debug("write::DONE::Writing to client " + str);
//   }


    public void run() {
        try{
            ObjectCacheRemote.gotNewConnection();
        } catch (Exception e){

        }


        try {
            String clientIP = ("" + client.getInetAddress()).substring(1);
            client.setKeepAlive(true);
            client.setSoTimeout(1000 * 5 * 1); // 1 mins
            client.setTcpNoDelay(true);

            log.debug("Got connection from " + clientIP);
//            output = client.getOutputStream();
            client.setKeepAlive( true );
            boolean run = true;
            String curLine = null;
            byte[] dataBytes = new byte[256];
            int byteLength = 0;
            StringTokenizer tok = null;
            input = client.getInputStream();
            String preline = null;
            while (run) {
                try{
                    byteLength = input.read(dataBytes, 0, dataBytes.length);
                } catch (java.net.SocketTimeoutException noProblem){
                    //no problem ... just didn't get any data within the timeframe
                    continue;
                }
                if (byteLength == -1) {
                    run = false;
                    break;
                }
                String readLine = new String(dataBytes, 0, byteLength);
                int indexOfN = readLine.indexOf("\n");
                if(indexOfN < 0){
                    preline = preline + readLine;
                } else {
                    curLine = ((preline != null ? preline : "") + readLine.substring(0, indexOfN)).trim();
                    preline = readLine.substring(indexOfN);
                    tok = new StringTokenizer(curLine, " \n\r\t:");
                    if (curLine.startsWith("r:")) { //remove
                        String command = tok.nextToken();
                        String clazzName = tok.nextToken();
                        String objectID = tok.nextToken();
                        log.debug("Parameters: clazz("+ clazzName +") objectID("+ objectID +")");
                        Class<?> aClass = Class.forName(clazzName);
                        ObjectCache objectCache = ObjectCacheFactory.getInstance().getObjectCache(aClass);
                        if(objectCache != null){
                            log.debug("Removing from cache: clazz("+ clazzName +") objectID("+ objectID +")");
                            objectCache.removeFromCache(objectID);
                        }
                    } else if(curLine.startsWith("ll:")){
                        String command = tok.nextToken();
                        String clazz = tok.nextToken();
                        String lockID = tok.nextToken();
                        GlobalLockService.getInstance().lockFromRemote(lockID);
                    } else if(curLine.startsWith("ul:")){
                        String command = tok.nextToken();
                        String clazz = tok.nextToken();
                        String lockID = tok.nextToken();
                        GlobalLockService.getInstance().unlockFromRemote(lockID);
                    } else {
                        log.error("Dont understand line: " + curLine);
    //                    write("-ERR Not implementet", output);
    //                    run = false;
                }
                }
            }

            output.flush();
            output.close();
            input.close();
            log.debug("Try to close client -normal");
            client.close();
        } catch (Exception e) {
            log.error("Some error in run() " + e.toString(), e);
            e.printStackTrace();
        } finally {
            try{
            if(output != null) output.close();
            } catch (Exception t){}
            try{
            if(input != null) input.close();
            } catch (Exception t){}
            try{
            if(client != null) client.close();
            } catch (Exception t){}
            client = null;
            output = null;
            input = null;

        }
    }

    public void close(){
        try{
        if(output != null) output.close();
        } catch (Exception t){}
        try{
        if(input != null) input.close();
        } catch (Exception t){}
        try{
        if(client != null) client.close();
        } catch (Exception t){}
        client = null;
        output = null;
        input = null;
    }


}







