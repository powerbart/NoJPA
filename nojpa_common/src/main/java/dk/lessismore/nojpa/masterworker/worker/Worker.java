package dk.lessismore.nojpa.masterworker.worker;

import dk.lessismore.nojpa.concurrency.WaitForValueRepeat;
import dk.lessismore.nojpa.masterworker.SystemHealth;
import dk.lessismore.nojpa.masterworker.bean.RemoteBeanInterface;
import dk.lessismore.nojpa.masterworker.bean.worker.BeanExecutor;
import dk.lessismore.nojpa.masterworker.exceptions.JobDoesNotExistException;
import dk.lessismore.nojpa.masterworker.exceptions.WorkerExecutionException;
import dk.lessismore.nojpa.masterworker.executor.Executor;
import dk.lessismore.nojpa.masterworker.master.MasterProperties;
import dk.lessismore.nojpa.masterworker.messages.HealthMessage;
import dk.lessismore.nojpa.masterworker.messages.JobMessage;
import dk.lessismore.nojpa.masterworker.messages.JobProgressMessage;
import dk.lessismore.nojpa.masterworker.messages.JobResultMessage;
import dk.lessismore.nojpa.masterworker.messages.KillMessage;
import dk.lessismore.nojpa.masterworker.messages.RegistrationMessage;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteBeanMessage;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteResultMessage;
import dk.lessismore.nojpa.masterworker.messages.StopMessage;
import dk.lessismore.nojpa.net.link.ClientLink;
import dk.lessismore.nojpa.properties.PropertiesProxy;
import dk.lessismore.nojpa.serialization.Serializer;
import dk.lessismore.nojpa.serialization.XmlSerializer;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class Worker {

    private static final MasterProperties properties = PropertiesProxy.getInstance(MasterProperties.class);
    private static final Logger log = LoggerFactory.getLogger(Worker.class);
    private static final long SEND_PROGRESS_INTERVAL = 10 * 1000;
    private static final long SEND_HEALTH_INTERVAL = 20 * 1000;
    private static final int  MAX_SAME_PROGRESS = 3;
    private final List<Class<? extends Executor>> supportedExecutors;
    private static final double CRITICAL_VM_MEMORY_USAGE = 0.95;
    private final Serializer serializer;
    private RemoteBeanInterface remoteBean = null;
    private Class<? extends RemoteBeanInterface> remoteBeanClass = null;
    private BeanExecutor beanExecutor = null;

    private boolean disableMemoryMonitoring = BooleanUtils.toBoolean(System.getenv("DISABLE_MEMORY_MONITORING"));
    private final LinkAndThreads linkAndThreads = new LinkAndThreads();

    private boolean stop = false;

    public Worker(Serializer serializer, List<Class<? extends Executor>> supportedExecutors) {
        this.serializer = serializer;
        this.supportedExecutors = supportedExecutors;
    }

    public Worker(Serializer serializer, Class<? extends Executor> ... supportedExecutors) {
        this(serializer, Arrays.asList(supportedExecutors));
    }

    public Worker(List<Class<? extends Executor>> supportedExecutors) {
        this (new XmlSerializer(), supportedExecutors);
    }

    public Worker(Class<? extends Executor> ... supportedExecutors) {
        this (new XmlSerializer(), supportedExecutors);
    }

    public Worker(Class<? extends RemoteBeanInterface> remoteBeanClass, RemoteBeanInterface remoteBean, Class<? extends Executor> ... supportedExecutors) {
        this (new XmlSerializer(), supportedExecutors);
        this.remoteBeanClass = remoteBeanClass;
        this.remoteBean = remoteBean;
    }

    public Worker(Class<? extends RemoteBeanInterface> remoteBeanClass, Class<? extends Executor>[] supportedExecutors, RemoteBeanInterface remoteBean) {
        this(remoteBeanClass, remoteBean, supportedExecutors);
    }


    /**
     * Runs one job and then exits if memory usage is critical high.
     * It should be run as it's own process.
     * It is supposed to be run in a loop (probably a batch script) that restarts it when it quits.
     */
    public void run() {
        String host = properties.getHost();
        int port = properties.getWorkerPort();

        while(true && !stop) {
            //final ClientLink clientLink;

            if(linkAndThreads.clientLink == null) {
                log.debug("1/2:Worker trying to establish connection to Master on "+host+":"+port);
                long start = System.currentTimeMillis();
                try {
                    linkAndThreads.clientLink = new ClientLink(host, port);
                    linkAndThreads.clientLink.write(new RegistrationMessage(remoteBeanClass, getSupportedExecutors()));
                    linkAndThreads.startThreads();
                    if(remoteBean != null){
                        beanExecutor = new BeanExecutor(remoteBeanClass, remoteBean);
                    }
                } catch (Exception e) {
                    log.error("Failed to connect to Master on " + host + ":" + port, e);
                    try {
                        log.error("Will now sleep for 10 secs");
                        Thread.sleep(10_000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    log.error("Will now call System.exit - since we can't connect to Master .... ");
                    System.exit(-1);

                }
                log.debug("2/2:Worker trying to establish connection to Master on "+host+":"+port + " TIME("+ (System.currentTimeMillis() - start) +")");
            }


            log.debug("Waiting for job");
            final JobMessage jobMessage = linkAndThreads.waitForValue.getValue();
            log.debug("Got a job...!");
            if(jobMessage == null){
                String message = "Exception .. jobMessage == null";
                log.error(message, new Exception(message));
                stop = true;
                linkAndThreads.stopThreads();
                break;
            }
            linkAndThreads.executor = loadExecutor(jobMessage);
            final Object input = serializer.unserialize(jobMessage.getSerializedJobData());

            final JobResultMessage<Object> resultMessage = new JobResultMessage<Object>(jobMessage.getJobID());

            log.debug("Job received... jobMessage.getJobID("+ jobMessage.getJobID() +")");

            linkAndThreads.jobThread = new Thread(new Runnable() {
                public void run() {
                    runJob(linkAndThreads.executor, input, resultMessage);
                }
            });
            linkAndThreads.jobThread.setDaemon(true);
            linkAndThreads.jobThread.start();

            log.debug("Job running...");


            log.debug("Waiting for calculation to end...");

            double progress = -1.0;
            int sameProgress = 0;
            while(linkAndThreads.jobThread != null && linkAndThreads.clientLink != null && linkAndThreads.jobThread.isAlive() && linkAndThreads.clientLink.isWorking()) {
                if(remoteBean == null) {
                    if (linkAndThreads.executor.getProgress() != progress) {
                        sameProgress = 0;
                        progress = linkAndThreads.executor.getProgress();
                        log.debug("Working: " + (progress * 100) + "%");
                        try {
                            linkAndThreads.clientLink.write(new JobProgressMessage(jobMessage.getJobID(), progress));
                        } catch (IOException e) {
                            log.error("IOException while writing back progress. Closing link", e);
                            linkAndThreads.clientLink.close();
                            break;
                        }
                    } else {
                        sameProgress++;
                        if (sameProgress > MAX_SAME_PROGRESS) {
                            log.debug("This is a job that haven't had any progress the last " + (sameProgress * 10) + "sec - we will kill it...");
                            resultMessage.setException(new WorkerExecutionException("Too long time with no progress"), serializer);
                            break;
                        }
                    }
                }
                try {
                    log.debug("sameProgress: " + sameProgress);
                    log.debug("Will: linkAndThreads.jobThread.join(SEND_PROGRESS_INTERVAL); - START");
                    linkAndThreads.jobThread.join(SEND_PROGRESS_INTERVAL);
                    log.debug("Will: linkAndThreads.jobThread.join(SEND_PROGRESS_INTERVAL); - END");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(linkAndThreads.clientLink != null) {
                log.debug("Writing back result...");
                try {
                    linkAndThreads.clientLink.write(resultMessage);
                } catch (IOException e) {
                    log.error("Error when writing job result to master: ", e);
                    linkAndThreads.clientLink.close();
                    linkAndThreads.stopThreads();
                    break;
                }

                if (!disableMemoryMonitoring && SystemHealth.getVmMemoryUsage() > CRITICAL_VM_MEMORY_USAGE) {
                    log.warn("Worker has a critical high VM memory usage.");
                    stop = true;
                    linkAndThreads.clientLink.close();
                    linkAndThreads.stopThreads();
                    break; //exit
                }

                if (sameProgress > MAX_SAME_PROGRESS) {
                    stop = true;
                    break;
                }
            }
//            stop = true;
//            break;

        } // end loop
        log.debug("Exiting!-1");
        try{
            linkAndThreads.clientLink.close();
            linkAndThreads.stopThreads();
        } catch (Exception e){}
        log.debug("Exiting!-2");
    }

    private Executor<?, ?> loadExecutor(JobMessage jobMessage) {
        try {
            final String className = jobMessage.getExecutorClassName();
            if(remoteBeanClass != null && remoteBeanClass.getName().equals(className)){
                return beanExecutor;
            } else {
                final Class executorClass = Class.forName(className);
                return (Executor) executorClass.newInstance();
            }
        } catch (Exception e) {
            log.error("Error extracting executer from job message. ", e);
            throw new RuntimeException(e);
        }
    }

    private void runJob(Executor<Object, Object> executor, Object input, JobResultMessage<Object> resultMessage) {
        try {
            log.debug("runJob:Will run job: " + input);
            Object result = executor.run(input);
            log.debug("runJob:result("+ result +")");
            if(executor.isStoppedNicely()){
                log.debug("runJob:executor.isStoppedNicely("+ executor.isStoppedNicely() +") - will return exception");
                resultMessage.setException(new JobDoesNotExistException(), serializer);
            } else {
                log.debug("runJob:setResult...");
                resultMessage.setResult(result, serializer);
            }
        } catch (Exception e) {
            resultMessage.setException(e, serializer);
        }
    }

    private List<Class<? extends Executor>> getSupportedExecutors() {
        return supportedExecutors;
    }


    protected class LinkAndThreads {

        protected ClientLink clientLink = null;
        protected Thread healthReporterThread = null;
        protected Thread stopperThread = null;
        protected WaitForValueRepeat<JobMessage> waitForValue = new WaitForValueRepeat<JobMessage>();
        protected Thread jobThread = null;
        protected Executor executor = null;
        protected JobMessage maybeJob = null;


        public void stopThreads(){
            if(stopperThread != null){
                try {
                    stopperThread.interrupt();
                } catch (Exception e){}
            }
            if(healthReporterThread != null){
                try {
                    healthReporterThread.interrupt();
                } catch (Exception e){}
            }
        }

        public void startThreads(){
            healthReporterThread = new Thread(new Runnable() {
                public void run() {
                    int countOfFails = 0;
                    while(!stop && linkAndThreads.clientLink != null) {
                        try {
                            HealthMessage healthMessage = new HealthMessage(SystemHealth.getSystemLoadAverage(),
                                    SystemHealth.getVmMemoryUsage(), SystemHealth.getDiskUsages());
                            if(linkAndThreads.clientLink != null && linkAndThreads.clientLink.isWorking()) {
                                linkAndThreads.clientLink.write(healthMessage);
                            }
                            if(countOfFails > 3){
                                log.error("WE WILL CLOSE DOWN IN HEALT.. BECAUSE OF ERRORS .... System.exit");
                                System.exit(-1);
                            }
                            Thread.sleep(SEND_HEALTH_INTERVAL);
                        } catch (IOException e) {
                            log.debug("IOException - shutting down healthReporterThread");
                            break;
                        } catch (InterruptedException e) {
//                            log.debug("Health report thread interrupted");
                        } catch (Exception e) {
                            log.error("Exception in healthReporterThread: " + e, e);
                            log.error("Will now call System.exit");
                            System.exit(-1);
                        }
                    }
                }
            });
            healthReporterThread.setDaemon(true);
            healthReporterThread.start();



            stopperThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        Calendar lastMsg = Calendar.getInstance();
                        while(!stop && linkAndThreads.clientLink.isWorking()) { //TODO loop unnecassary
                            Object o = linkAndThreads.clientLink.read();
                            if(o instanceof JobMessage) {
                                log.debug("Read: JobMessage");
                                maybeJob = (JobMessage) o;
                                waitForValue.setValue(maybeJob);
                            } else if(o instanceof KillMessage) {
                                KillMessage msg = (KillMessage) o;
                                Calendar oneMin = Calendar.getInstance();
                                oneMin.add(Calendar.MINUTE, -1);
                                if(lastMsg.before(oneMin)) {
                                    log.error("Kill message recieved from master - shutting down. " + msg.getJobID());
                                    log.error("Kill message recieved from master - shutting down. " + msg.getJobID());
                                    log.error("Kill message recieved from master - shutting down. " + msg.getJobID());
                                    log.error("Kill message recieved from master - shutting down. " + msg.getJobID());
                                    log.error("Kill message recieved from master - shutting down. " + msg.getJobID());
                                    stop = true;
                                    System.exit(0);
                                } else {
                                    log.info("We will ignore Kill message, since last msg was ("+ lastMsg.getTime() +") < 1 min ago");
                                    executor.stopNicely();
                                }
                            } else if(o instanceof StopMessage) {
                                log.info("Stop message recieved from master - signal executer to stop nicely.");
                                executor.stopNicely();
                            } else if(o instanceof RunMethodRemoteBeanMessage) {
                                RunMethodRemoteBeanMessage runMethodRemoteBeanMessage = (RunMethodRemoteBeanMessage) o;
                                BeanExecutor b = (BeanExecutor) executor;
                                Object resultOfMethod = null;
                                RunMethodRemoteResultMessage resultMessageOfMethod = new RunMethodRemoteResultMessage(maybeJob.getJobID());
                                resultMessageOfMethod.setMethodID( runMethodRemoteBeanMessage.getMethodID() );
                                resultMessageOfMethod.setMethodName( runMethodRemoteBeanMessage.getMethodName() );
                                try{
                                    resultOfMethod = b.runMethod(runMethodRemoteBeanMessage);
                                    log.debug("Will set the of " + runMethodRemoteBeanMessage.getMethodName() + " -> result("+ (resultOfMethod == null ? "NULL" : "HAS-RESULT") +") ");
//                                    if(resultOfMethod != null){ // TODO ??? - should we return the null?

                                    resultMessageOfMethod.setResult(resultOfMethod, serializer);
                                    try{
//                                            log.debug("Writing the result : " + resultMessageOfMethod);
                                        linkAndThreads.clientLink.write(resultMessageOfMethod);
                                    } catch (Exception e){
                                        log.error("Some error when writing back result ... Have been executed .. " + e, e);
                                    }
//                                    }
                                } catch(Exception t){
                                    log.error("Some error when calling remoteMethod: " + t, t);
                                    resultMessageOfMethod.setException( t , serializer);
                                    try{
                                        linkAndThreads.clientLink.write(resultMessageOfMethod);

                                    } catch (Exception e){
                                        log.error("Some error when writing back result ... Have been executed .. " + e, e);
                                    }
                                }
                                System.out.println("We will now run RemoteMethod on executor :" + executor);
                            } else {
                                log.error("Did not understand message from master (stop or kill message expected): " + o);
                            }
                            lastMsg = Calendar.getInstance();
                        }
                    } catch(ClosedChannelException e) {
                        log.error("Connection closed - Stopping stopperThread");
                    } catch(IOException e) {
                        log.error("Some error in stopper jobThread: ", e);
                    } finally {
                        try{
                            log.error("WE WILL CLOSE DOWN AND EXIT-1");
                            linkAndThreads.clientLink.close();
                        } catch (Exception e){
                        }
                        log.info("We are done..... ");
                        log.error("WE WILL CLOSE DOWN AND EXIT-2");
                        if(linkAndThreads.clientLink != null){
                            try {
                                linkAndThreads.clientLink.close();
                                linkAndThreads.clientLink = null;
                            } catch (Exception e){
                            }
                        } else {
                        }
                        log.error("WE WILL CLOSE DOWN AND EXIT-3 .... System.exit");
                        log.error("WE WILL CLOSE DOWN AND EXIT-3 .... System.exit");
                        log.error("WE WILL CLOSE DOWN AND EXIT-3 .... System.exit");
                        System.exit(-1);
                    }
                }
            });
            stopperThread.setDaemon(true);
            stopperThread.start();


        }


    }


}
