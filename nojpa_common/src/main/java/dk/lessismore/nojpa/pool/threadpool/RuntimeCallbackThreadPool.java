package dk.lessismore.nojpa.pool.threadpool;

import dk.lessismore.nojpa.guid.GuidFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: bart
 * Date: 30-05-2007
 * Time: 21:23:26
 */
public class RuntimeCallbackThreadPool {

    protected final static Log log = LogFactory.getLog(RuntimeCallbackThreadPool.class);


    private final int countOfThreads;
    private boolean runing = true;
    private final LinkedList<CalcWork> workToDo = new LinkedList<CalcWork>();
    private final LinkedList<CallbackWorker> workers;
    private final LinkedList<RuntimeCallbackCalcInterface> unbizyResources;
    private final Class classOfCallbackCalcInterface;

    public RuntimeCallbackThreadPool(int countOfThreads, Class classOfCallbackCalcInterface) throws IllegalAccessException, InstantiationException {
        this.classOfCallbackCalcInterface = classOfCallbackCalcInterface;
        this.countOfThreads = countOfThreads;
        workers = new LinkedList<CallbackWorker>();
        unbizyResources = new LinkedList<RuntimeCallbackCalcInterface>();
        for(int i = 0; i < countOfThreads; i++){
            unbizyResources.add((RuntimeCallbackCalcInterface) classOfCallbackCalcInterface.newInstance());
        }

    }


    public Object doCalc(Object input) throws Exception {
        CalcWork work = new CalcWork(Thread.currentThread());
        work.input = input;
        doCalcWork(work);
        int countOfWait = 0;
        while (!work.getDone()) {
            try {
                synchronized (Thread.currentThread()) {
                    countOfWait++;
                    Thread.currentThread().wait(8 * 1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(countOfWait > 3){
                try {
                    synchronized (unbizyResources) {
                        if (!unbizyResources.isEmpty()) {

                            synchronized (workers) {
                                if (workers.size() < countOfThreads) {
                                    CallbackWorker worker = new CallbackWorker();
                                    worker.setName("Callback-Worker");
                                    synchronized (workers) {
                                        workers.addLast(worker);
                                    }
                                    worker.start();
                                }
                            }
                        } else {

                        }
                    }
                } catch (Exception e) {

                }
            }
        }
        if (work.exception != null) {
            throw work.exception;
        } else {
            return work.output;
        }
    }

    private void doCalcWork(CalcWork work) {
        synchronized(workToDo){
            workToDo.addLast(work);
        }
        try{
            boolean isUnbizyResEmpty;
            synchronized(unbizyResources){
                isUnbizyResEmpty = unbizyResources.isEmpty();
            }
            if(isUnbizyResEmpty){
                //log.debug("Reuse thread");
                synchronized(workers){
                    for(Iterator<CallbackWorker> iterator = workers.iterator(); iterator.hasNext(); ){
                        RuntimeCallbackThreadPool.CallbackWorker worker = iterator.next();
                        synchronized(worker){
                            if(worker.getState() == Thread.State.WAITING){
                                worker.notify();
                            }
                        }
                    }
                }
            } else {
                CallbackWorker worker = new CallbackWorker();
                worker.setName("Callback-Worker");
                synchronized(workers){
                    workers.addLast(worker);
                }
                worker.start();
            }

        } catch(Exception e){
            e.printStackTrace();
        }
    }


    public class CallbackWorker extends Thread {
        public CallbackWorker(){
        }

        public void run(){
            int countOfWait = 0;
            while(runing && countOfWait < 10){
                try{
                    RuntimeCallbackCalcInterface calculator = null;
                    synchronized(unbizyResources){
                        if(!unbizyResources.isEmpty()){
                            calculator = unbizyResources.removeFirst();
                        }
                    }
                    if(calculator != null){
                        CalcWork work = null;
                        synchronized(workToDo){
                            if(!workToDo.isEmpty()){
                                work = workToDo.removeFirst();
                            }
                        }
                        if(work != null){
                            countOfWait = 0;
                            try{
                                work.output = calculator.doCalc(work.input);
                            } catch(Exception e){
                                work.exception = e;
                            } finally{
                                synchronized(unbizyResources){
                                    unbizyResources.addLast(calculator);
                                }
                                work.setDone(true);
                                try{
                                    synchronized(work.meToNotify){
                                        work.meToNotify.notify();
                                    }
                                } catch (Exception ne){
                                    ne.printStackTrace();
                                }
                            }
                        } else {
                            synchronized(unbizyResources){
                                unbizyResources.addLast(calculator);
                            }
                            synchronized(this){
                                this.wait(1000);
                                log.debug(Thread.currentThread().getName() + "::countOfWait = " + countOfWait);
                                countOfWait++;
                            }
                        }
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
            synchronized(workers){
                workers.remove(this);
            }
        }
    }

    protected class CalcWork {
        public Object input = null;
        public Object output = null;
        public Exception exception = null;
        public final Thread meToNotify;
        public String id = GuidFactory.getInstance().makeGuid();
        private boolean done = false;

        public CalcWork(Thread meToNotify){
            this.meToNotify = meToNotify;
        }


        public synchronized boolean getDone() {
            return done;
        }

        public synchronized void setDone(boolean done) {
            this.done = done;
        }
    }


    protected void finalize() throws Throwable {
        super.finalize();
        log.debug("Pool.finalize()");
    }
}
