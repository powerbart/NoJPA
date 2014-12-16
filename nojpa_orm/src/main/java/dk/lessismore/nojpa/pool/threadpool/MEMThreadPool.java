package dk.lessismore.nojpa.pool.threadpool;

import dk.lessismore.reusable_v3.log.Logger;
import dk.lessismore.reusable_v3.log.LoggerFactory;

import java.io.File;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: bart
 * Date: 26-09-2007
 * Time: 00:23:03
 */
public class MEMThreadPool<E extends ThreadPoolJob> {

    private final static Logger log = LoggerFactory.getInstance(MEMThreadPool.class);


    Thread workers = null;
    Class<? extends ThreadPoolWorker> workerClazz = null;
    LinkedList<E> jobs = new LinkedList<E>();
    boolean running = true;
    Thread[] myWorkers = null;


    public MEMThreadPool(Class<? extends ThreadPoolWorker> workerClazz, int countOfWorkers){
        this.workerClazz = workerClazz;
        myWorkers = new Thread[countOfWorkers];
        for(int i = 0; i < countOfWorkers; i++){
            myWorkers[i] = new MyWorker();
            myWorkers[i].start();
        }


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
                        log.debug("Do work");
                        workerClazz.newInstance().doWork(job);
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
