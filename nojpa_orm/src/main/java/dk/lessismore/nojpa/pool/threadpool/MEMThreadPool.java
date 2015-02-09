package dk.lessismore.nojpa.pool.threadpool;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: bart
 * Date: 26-09-2007
 * Time: 00:23:03
 */
public class MEMThreadPool<E extends ThreadPoolJob> {

    protected final static Log log = LogFactory.getLog(MEMThreadPool.class);


    Thread workers = null;
    Class<? extends ThreadPoolWorker> workerClazz = null;
    LinkedList<E> jobs = new LinkedList<E>();
    boolean running = true;
    Thread[] myWorkers = null;
    long maxRunningTimeInMillis = 0;


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
            log.debug("Will now check id("+ myWorker.getName() +")/oldID("+ oldID +") myWorker.isAlive("+ myWorker.isAlive() +")");
            if(myWorker.isAlive() && myWorker.getName().equals(oldID)){
                log.debug("Will now KILL "+ myWorker.getName());
                myWorker.interrupt();
                for(int i = 0; i < myWorkers.length; i++){
                    if(myWorker.equals(myWorkers[i])) {
                        myWorkers[i] = new MyWorker();
                        myWorkers[i].start();
                        myWorker.setName("Killed-it-" + myWorker.getName());
                        break;
                    }
                }
            }
        }
    }

    protected class MyWorker extends Thread {



        public void run() {
            while(running){
                try{
                E job = getNextJob();
                if(job == null){
                    try {
                        setName("MEMThreadPool-Worker");
                        synchronized(this){
                            wait(50);
                        }
                    } catch (InterruptedException e) {

                    }
                } else {
                    try {
                        setName("MEMThreadPool-Worker-" + (new File(job.fullPathToStoreFile)).getName());

                        if(maxRunningTimeInMillis > 0){
                            WatchWorker watchWorker = new WatchWorker(this);
                            watchWorker.setName("WatchWorker: " + getName());
                            watchWorker.start();
                        }


                        log.debug("Do work - STARTS " + getName());
                        workerClazz.newInstance().doWork(job);
                        log.debug("Do work - ENDS "  + getName());
                    } catch (Exception e) {
                        log.error("Some error :: " +e, e);
                    }
                }
                } catch(Exception e){
                    log.error("Some error " +e, e);
                }
            }



        }
    }











}
