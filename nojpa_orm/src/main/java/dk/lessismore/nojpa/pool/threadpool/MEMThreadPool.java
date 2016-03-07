package dk.lessismore.nojpa.pool.threadpool;


import dk.lessismore.nojpa.utils.SuperIO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: bart..
 * Date: 26-09-2007
 * Time: 00:23:03
 */
public class MEMThreadPool<E extends ThreadPoolJob> {

    protected final static Log log = LogFactory.getLog(MEMThreadPool.class);


    protected Thread workers = null;
    protected Class<? extends ThreadPoolWorker> workerClazz = null;
    protected LinkedList<E> jobs = new LinkedList<E>();
    protected boolean running = true;
    protected Thread[] myWorkers = null;
    protected long maxRunningTimeInMillis = 0;


    int numberOfRunningThread = 0;


    public MEMThreadPool(Class<? extends ThreadPoolWorker> workerClazz, int countOfWorkers, long maxRunningTimeInMillis){
        this.workerClazz = workerClazz;
        myWorkers = new Thread[countOfWorkers];
        for(int i = 0; i < countOfWorkers; i++){
            myWorkers[i] = new MyWorker();
            myWorkers[i].start();
        }
        this.maxRunningTimeInMillis = maxRunningTimeInMillis;

    }
    public MEMThreadPool(Class<? extends ThreadPoolWorker> workerClazz, int countOfWorkers){
        this(workerClazz, countOfWorkers, 0);

    }

    protected synchronized E getNextJob(){
        if(!jobs.isEmpty()){
            return jobs.removeFirst();
        } else {
            return null;
        }
    }

    public synchronized void addJob(E job){
        jobs.add(job);
        synchronized(myWorkers[0]){
            myWorkers[0].notifyAll();
        }
        
    }

    public int getNumberOfRunningThreads(){
        int result = 0;
        for(int i = 0; i < myWorkers.length; i++){
            if(((MyWorker)myWorkers[i]).doingWork){
                result++;
            }
        }
        return result;
    }


    public synchronized boolean isDone(){
        return jobs.isEmpty();
    }

    public synchronized int size(){
        return jobs.size();
    }

    public void stop(){
        running = false;
    }


    protected class WatchWorker extends Thread {
        private final MyWorker myWorker;

        public WatchWorker(MyWorker myWorker){
            this.myWorker = myWorker;
        }


        public void run(){
            String oldID = "" + myWorker.getName();
            try {
                this.sleep(maxRunningTimeInMillis);
            } catch (InterruptedException e) {
            }
//            log.debug("Will now check id("+ myWorker.getName() +")/oldID("+ oldID +") myWorker.isAlive("+ myWorker.isAlive() +")");
            if(myWorker.isAlive() && myWorker.getName().equals(oldID)){
                log.debug("Will now KILL "+ myWorker.getName());
                myWorker.interrupt();
                for(int i = 0; i < myWorkers.length; i++){
                    if(myWorker.equals(myWorkers[i])) {
                        myWorkers[i] = new MyWorker();
                        myWorkers[i].start();
                        myWorker.setName("Killed-it-" + myWorker.getName());
                        myWorker.shouldRun = false;
                        break;
                    }
                }
            }
        }
    }

    static int ids = 0;
    protected class MyWorker extends Thread {

        boolean shouldRun = true;
        boolean doingWork = false;

        final int id;

        public MyWorker(){
            id = ids;
        }


        public void run() {
            int debugCounter = 0;
            while(running && shouldRun){
                try{
                    if(++debugCounter % 5000 == 0){
                        log.debug("Checking for new job debugCounter("+ debugCounter +")");
                    }
                E job = getNextJob();
                if(job == null){
                    try {
                        setName("MEMThreadPool-Worker-"+ id);
                        synchronized(this){
                            wait(100);
                        }
                    } catch (InterruptedException e) {

                    }
                } else {
                    setName("W_"+ id + "_" + Calendar.getInstance().get(Calendar.HOUR) + "_"  + (new File(job.fullPathToStoreFile)).getName());
                    File statusFile = new File(job.fullPathToStoreFile);
                    try {
                        SuperIO.writeTextToFile(statusFile, "" + new Date());
                        doingWork = true;
                        if(maxRunningTimeInMillis > 0){
                            WatchWorker watchWorker = new WatchWorker(this);
                            watchWorker.setName("WatchWorker: " + getName());
                            watchWorker.start();
                        }
                        log.debug("Do work - STARTS " + getName());
                        workerClazz.newInstance().doWork(job);
                        log.debug("Do work - ENDS "  + getName());
                    } catch (java.lang.OutOfMemoryError e) {
                        log.error("Some error OutOfMemoryError:: " +e, e);
                        shouldRun = false;
                        for(int i = 0; i < myWorkers.length; i++){
                            if (this.equals(myWorkers[i])) {
                                myWorkers[i] = new MyWorker();
                                myWorkers[i].start();
                                this.setName("Killed-it-" + this.getName());
                                this.shouldRun = false;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.error("Some error :: " +e, e);
                    } finally {
                        statusFile.delete();
                        doingWork = false;
                    }
                }
                } catch(Exception e){
                    log.error("Some error " +e, e);
                }
            }



        }
    }











}
