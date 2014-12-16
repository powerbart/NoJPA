package dk.lessismore.nojpa.pool.threadpool;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import dk.lessismore.reusable_v3.guid.GuidFactory;
import dk.lessismore.reusable_v3.log.Logger;
import dk.lessismore.reusable_v3.log.LoggerFactory;
import dk.lessismore.reusable_v3.utils.SuperIO;

import java.io.File;
import java.util.ArrayList;

/**
 * User: bart
 * Date: 22-04-2007
 * Time: 23:24:29
 */
public class ThreadPool {

    private final static Logger log = LoggerFactory.getInstance(ThreadPool.class);

    private File storeJobDir = null;
    private int threadMaxSize = 10;
    private Class threadWorkerClass = null;
    private boolean running = true;
    private static XStream xStream = getXStream();
    private final ArrayList<WorkerThread> workers = new ArrayList<WorkerThread>();
    private final ArrayList<WorkerThread> notWorking = new ArrayList<WorkerThread>();
    private final ArrayList<ThreadPoolJob> jobsTodo = new ArrayList<ThreadPoolJob>();
    private final ArrayList<ThreadPoolJob> runningJobs = new ArrayList<ThreadPoolJob>();

    public ThreadPool(File storeJobDir, int threadMaxSize, Class threadWorkerClass){
        this.storeJobDir = storeJobDir;
        this.threadMaxSize = threadMaxSize;
        this.threadWorkerClass = threadWorkerClass;
        loadOldJobs();
        for(int i = 0; i < threadMaxSize; i++){
            log.debug("Constructor: creating worker");
            WorkerThread w = new WorkerThread();
            w.setPriority(Thread.MIN_PRIORITY);
            w.setName("WorkerOf" + threadWorkerClass.getSimpleName() + "-" + i);
            synchronized(workers){
                workers.add(w);
                w.start();
            }
        }
    }

    public int jobsTodoSize(){
        synchronized (jobsTodo) {
            return jobsTodo.size();
        }
    }

    public void doJob(ThreadPoolJob job){
        try{
            log.debug("doJob::START STAT:[workers.size("+ workers.size() +") notWorking.size("+ notWorking.size() +") jobsTodo.size("+ jobsTodo.size() +") runningJobs.size("+ runningJobs.size() +") ] v.2011apr06 16:15");
        } catch (Exception e){ e.printStackTrace(); }
        storeJob(job);
        addJob(job);
        log.debug("doJob::ENDS::");
    }


    protected class WorkerThread extends Thread {

        private final Logger log = LoggerFactory.getInstance(WorkerThread.class);

        public void run(){
            while(running){
                try {
                    //log.debug("run::Try to get work");
                    ThreadPoolJob job = getWorkJob();
                    //log.debug("run:: job = " + job);
                    if(job != null){
                        ThreadPoolWorker worker = (ThreadPoolWorker) threadWorkerClass.newInstance();
                        //log.debug("run::START working with job = " + job);
                        worker.doWork(job);
                        //log.debug("run::DONE working with job = " + job);
                        worker = null;
                        doneWithJob(job);
                    } else {
                        noMoreWork(this);
                    }
                } catch (Exception e) {
                    log.error("Some error in run : " + e, e);
                    e.printStackTrace();
                }
            }
        }


    }

    private void doneWithJob(ThreadPoolJob job) {
        //log.debug("doneWithJob::START");
        //log.debug("doneWithJob - sync -start");
        synchronized(runningJobs){
            //log.debug("doneWithJob - sync -ends");
            runningJobs.remove(job);
            //log.info("doneWithJob::  jobsTodo:" + jobsTodo.size());
        }
        (new File(job.fullPathToStoreFile)).delete();
        //log.debug("doneWithJob::ENDS::");
    }

    private ThreadPoolJob getWorkJob() {
        //log.debug("getWorkJob::START");
        ThreadPoolJob job = null;
        //log.debug("getWorkJob - sync -starts");
        synchronized(jobsTodo){
            //log.debug("getWorkJob - sync -ends");
            if(jobsTodo.isEmpty()){
                //log.debug("getWorkJob no work ...");
                //log.debug("getWorkJob::ENDS::1");
                return null;
            } else {
                job = jobsTodo.get(0);
                jobsTodo.remove(job);
            }
        }
        //log.debug("getWorkJob:runningJobs - sync -starts");
        synchronized(runningJobs){
            //log.debug("getWorkJob:runningJobs - sync -ends");
            runningJobs.add(job);
            //log.debug("getWorkJob retuns runningJobs = " + runningJobs.size());
            //log.debug("getWorkJob::ENDS::2");
            return job;
        }

    }

    private void noMoreWork(WorkerThread workerThread) {
        //log.debug("noMoreWork::sync - starts");
        synchronized (workerThread) {
            //log.debug("noMoreWork::sync - ends");
            try {
                //log.debug("noMoreWork sync - starts workerThread = " + workerThread);
                synchronized (notWorking) {
                    //log.debug("noMoreWork sync - ends workerThread = " + workerThread);
                    if(!notWorking.contains(workerThread)){
                        notWorking.add(workerThread);
                    }
                }
                workerThread.wait(5 * 1000);
            } catch (InterruptedException e) {

            }
        }
        //log.debug("noMoreWork::ENDS::");
    }


    private void addJob(ThreadPoolJob job) {
        //log.debug("addJob::START");
        log.debug("addJob job = " + job);
        //log.debug("addJob:: sync - starts");
        synchronized(jobsTodo){
            //log.debug("addJob::sync - ends");
            jobsTodo.add(job);
            //log.debug("addJob::3");
            //log.info("addJob::  jobsTodo:" + jobsTodo.size());
            //log.debug("addJob::4");
        }
        ThreadPool.WorkerThread worker = null;
        //log.debug("addJob::5 - sync - -starts");
        synchronized(notWorking){
            //log.debug("addJob::6 - sync - ends");
            if(!notWorking.isEmpty()){
                //log.debug("addJob::7");
                worker = notWorking.get(0);
                if(worker != null){
                    notWorking.remove(worker);
                }
            }
            if(worker != null){
                //log.debug("addJob::9 - sync - starts");
                synchronized(worker){
                    //log.debug("addJob::sync - ends");
                    worker.notify();
                    //log.debug("addJob::10");
                }
            }
        }

        //log.debug("addJob::ENDS::");
    }


    private void loadOldJobs() {
        log.debug("loadOldJobs::START");
        if (!storeJobDir.exists()) {
            storeJobDir.mkdirs();
        }
        File[] files = storeJobDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            try {
                String xml = SuperIO.readTextFromFile(files[i]);
                ThreadPoolJob job = (ThreadPoolJob) fromXml(xml);
                //log.debug("loadOldJobs::sync - starts");
                synchronized (jobsTodo) {
                    //log.debug("loadOldJobs::sync - ends");
                    jobsTodo.add(job);
                }
                //addJob(job);
            } catch (Exception e) {
                log.error("loadOldJobs : some exception in " + files[i].getName() + " " + e, e);
                e.printStackTrace();
            }
        }
        log.debug("loadOldJobs::ENDS::");
    }


    private void storeJob(ThreadPoolJob job) {
        //log.debug("storeJob::START");
        log.debug("storeJob job = " + job);
        String jobName = GuidFactory.getInstance().makeGuid();
        String fullPathToStoreFile = storeJobDir.getAbsolutePath() + "/" + jobName;
        job.fullPathToStoreFile = fullPathToStoreFile;
        SuperIO.writeTextToFile(fullPathToStoreFile, toXml(job));
        //log.debug("storeJob::ENDS::");
    }




    protected static XStream getXStream() {
        XStream xStream = new XStream(new DomDriver());
        return xStream;
    }

    protected String toXml(Object object) {
        return xStream.toXML(object);
    }

    protected Object fromXml(String xmlString) {
        return xStream.fromXML(xmlString);
    }



}
