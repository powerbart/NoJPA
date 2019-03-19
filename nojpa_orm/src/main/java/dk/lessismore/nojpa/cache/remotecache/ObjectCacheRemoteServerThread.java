package dk.lessismore.nojpa.cache.remotecache;

import dk.lessismore.nojpa.cache.GlobalLockService;
import dk.lessismore.nojpa.cache.ObjectCache;
import dk.lessismore.nojpa.cache.ObjectCacheFactory;
import dk.lessismore.nojpa.cache.ObjectCacheRemote;
import dk.lessismore.nojpa.utils.MaxSizeArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ObjectCacheRemoteServerThread extends Thread {

    private static final Logger log = LoggerFactory.getLogger(ObjectCacheRemoteServerThread.class);

    static MaxSizeArray<ObjectCacheRemoteServerThread> allCurrentThreads = new MaxSizeArray<ObjectCacheRemoteServerThread>(32);

    public static void closeAll() {
        for (Iterator<ObjectCacheRemoteServerThread> iterator = allCurrentThreads.iterator(); iterator.hasNext(); ) {
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


    long lastRemoveCounter = -1;
    long lastLockCounter = -1;
    long lastUnlockCounter = -1;

    private void validateRemoveCounter(String number){
        try{
            long n = Long.parseLong(number);
            if(lastRemoveCounter == -1){
                log.debug("validateRemoveCounter: first number is " + lastRemoveCounter);
            } else {
                if(n == lastRemoveCounter + 1){
                    log.debug("validateRemoveCounter: FINE("+ n +") " + lastRemoveCounter);
                } else {
                    log.error("validateRemoveCounter: EXPECTED(" + (lastRemoveCounter + 1) +") GOT("+ n +")");

                }
            }
            lastRemoveCounter = n;
        } catch (Exception e){
            log.error("validateRemoveCounter " + e, e);
        }
    }

    private void validateLockCounter(String number){
        try{
            long n = Long.parseLong(number);
            if(lastLockCounter == -1){
                log.debug("validateLockCounter: first number is " + lastLockCounter);
            } else {
                if(n == lastLockCounter + 1){
                    log.debug("validateLockCounter: FINE("+ n +") " + lastLockCounter);
                } else {
                    log.error("validateLockCounter: EXPECTED(" + (lastLockCounter + 1) +") GOT("+ n +")");

                }
            }
            lastLockCounter = n;
        } catch (Exception e){
            log.error("validateLockCounter " + e, e);
        }
    }

    private void validateUnlockCounter(String number){
        try{
            long n = Long.parseLong(number);
            if(lastUnlockCounter == -1){
                log.debug("validateUnlockCounter: first number is " + lastUnlockCounter);
            } else {
                if(n == lastUnlockCounter + 1){
                    log.debug("validateUnlockCounter: FINE("+ n +") " + lastUnlockCounter);
                } else {
                    log.error("validateUnlockCounter: EXPECTED(" + (lastUnlockCounter + 1) +") GOT("+ n +")");

                }
            }
            lastUnlockCounter = n;
        } catch (Exception e){
            log.error("validateUnlockCounter " + e, e); //ERROE
        }
    }

    public void run() {
        try {
            ObjectCacheRemote.gotNewConnection();
        } catch (Exception e) {

        }

        String preline = null;
        String readLine = null;

        try {
            String clientIP = ("" + client.getInetAddress()).substring(1);
            client.setKeepAlive(true);
            client.setSoTimeout(1000 * 5 * 1); // 1 mins
            client.setTcpNoDelay(true);

            log.debug("Got connection from " + clientIP);
            output = client.getOutputStream();
            client.setKeepAlive(true);
            boolean run = true;
            String curLine = null;
            byte[] dataBytes = new byte[4096];
            int byteLength = 0;
            StringTokenizer tok = null;
            input = client.getInputStream();
            while (run) {
                try {
                    byteLength = input.read(dataBytes, 0, dataBytes.length);
                } catch (java.net.SocketTimeoutException noProblem) {
                    //no problem ... just didn't get any data within the timeframe
                    continue;
                }
                if (byteLength == -1) {
                    run = false;
                    break;
                }
                readLine = new String(dataBytes, 0, byteLength);

                //We have read something before....
                if(preline != null){
                    readLine = preline + readLine;
                    preline = null;
                }

                //We didn't read all of it
                if(readLine != null && readLine.length() < 32){
                    preline = readLine;
                    continue;
                }


//                log.debug("reading readLine("+ readLine.replaceAll("\n", "").trim() +")");
                StringTokenizer toks = new StringTokenizer(readLine, "\n");

                while (toks.hasMoreTokens()) {
                    curLine = toks.nextToken();
                    if(curLine.endsWith("\r")) {
                        curLine = curLine.trim();
                        StringTokenizer curLineToks = new StringTokenizer(curLine, ":");
                        if (curLine.startsWith("r:")) { //remove
                            String command = curLineToks.nextToken();
                            String number = curLineToks.nextToken();
                            validateRemoveCounter(number);
                            String clazzName = curLineToks.nextToken();
                            String objectID = curLineToks.nextToken();
                            log.debug("Parameters: clazz(" + clazzName + ") objectID(" + objectID + ")");
                            Class<?> aClass = Class.forName(clazzName);
                            ObjectCache objectCache = ObjectCacheFactory.getInstance().getObjectCache(aClass);
                            if (objectCache != null) {
                                log.debug("Removing from cache: clazz(" + clazzName + ") objectID(" + objectID + ")");
                                objectCache.removeFromCache(objectID);
                            }
                        } else if (curLine.startsWith("ll:")) {
                            String command = curLineToks.nextToken();
                            String number = curLineToks.nextToken();
                            validateLockCounter(number);
                            String lockID = curLineToks.nextToken();
                            log.debug("READING: lockFromRemote(" + lockID + ") ");
                            GlobalLockService.getInstance().lockFromRemote(lockID);
                        } else if (curLine.startsWith("ul:")) {
                            String command = curLineToks.nextToken();
                            String number = curLineToks.nextToken();
                            String lockID = curLineToks.nextToken();
                            validateUnlockCounter(number);
                            log.debug("READING: unlockFromRemote(" + lockID + ") ");
                            GlobalLockService.getInstance().unlockFromRemote(lockID);
                        } else if (curLine.startsWith("ml:")) {
                            String message = curLine.substring(3);
                            log.debug("READING: message(" + message + ") ");
                            GlobalLockService.getInstance().gotMessage(message);
                        } else {
                            log.error("Don't understand line: " + curLine);
                            run = false;
                            // write("-ERR Not implementet", output);
                            // run = false;
                        }
                    } else {
                        if(preline == null){
                            preline = curLine;
                        } else {
                            preline = preline + curLine;
                        }
                    }
                }
            }

            if (client.isConnected()) {
                output.flush();
                output.close();
                input.close();
                log.debug("Try to close client -normal");
                client.close();
            }
        } catch (Exception e) {
            log.error("Some error in run() readline("+ readLine +") ... preline("+ preline +")" + e.toString(), e);
        } finally {
            try {
                if (output != null) output.close();
            } catch (Exception t) {
            }
            try {
                if (input != null) input.close();
            } catch (Exception t) {
            }
            try {
                if (client != null) client.close();
            } catch (Exception t) {
            }
            client = null;
            output = null;
            input = null;

        }
    }

    public void close() {
        try {
            if (output != null) output.close();
        } catch (Exception t) {
        }
        try {
            if (input != null) input.close();
        } catch (Exception t) {
        }
        try {
            if (client != null) client.close();
        } catch (Exception t) {
        }
        client = null;
        output = null;
        input = null;
    }



}







