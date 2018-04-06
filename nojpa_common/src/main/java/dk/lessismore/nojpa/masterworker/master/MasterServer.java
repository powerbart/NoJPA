package dk.lessismore.nojpa.masterworker.master;

import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.messages.*;
import dk.lessismore.nojpa.masterworker.messages.observer.UpdateMessage;
import dk.lessismore.nojpa.net.link.ServerLink;
import dk.lessismore.nojpa.properties.PropertiesProxy;
import dk.lessismore.nojpa.serialization.Serializer;
import dk.lessismore.nojpa.serialization.XmlSerializer;
import dk.lessismore.nojpa.utils.SuperIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class MasterServer {

    private static final Logger log = LoggerFactory.getLogger(MasterServer.class);
    private static final MasterProperties properties = PropertiesProxy.getInstance(MasterProperties.class);

    private static boolean DEBUG = false;

    private final JobPool jobPool = new JobPool();
    private final WorkerPool workerPool = new WorkerPool();
    private final HashSet<ServerLink> observers = new HashSet<ServerLink>();
    private final Serializer storeSerializer = new XmlSerializer(); //TODO: We should just always use this serializer and stop having as argument...
    private final Calendar started = Calendar.getInstance();


    public void runMethodRemote(RunMethodRemoteBeanMessage runMethodRemoteBeanMessage, ServerLink serverLink) throws IOException {
        jobPool.runMethodRemote(runMethodRemoteBeanMessage, serverLink);
        notifyObservers();
    }



    //stopListen
    void clientClosedCancelRunningJobs(ServerLink client) {
        log.debug("clientClosedCancelRunningJobs: " + client);
        String jobID = jobPool.getJobID(client);

        log.debug("clientClosedCancelRunningJobs - jobID: " + jobID);
        if(jobID != null){
            log.debug("clientClosedCancelRunningJobs - sending KILL: " + jobID);
            cancelJob(jobID);
        } else {
            log.warn("We don't have a jobID for serverLink("+ client +")");
        }
//        jobPool.removeListener(client);
//        notifyObservers();
    }

    public void queueJob(JobMessage jobMessage, ServerLink client) {
        SuperIO.writeTextToFile("/tmp/masterworker_queue_size", "" + (jobPool.getQueueSize()));
        log.debug("queueJob("+ jobPool.getQueueSize() +"): " + jobMessage);
        jobPool.addJob(jobMessage, client);
        runJobIfNecessaryAndPossible();
        notifyObservers();
    }


    // Worker services
    public void registerWorker(RegistrationMessage registrationMessage , ServerLink serverLink) {
        SuperIO.writeTextToFile("/tmp/masterworker_worker_count", "" + (workerPool.getSize()));
        log.debug("registerWorker: " + serverLink);
        workerPool.addWorker(registrationMessage, serverLink);
        runJobIfNecessaryAndPossible();
        notifyObservers();
    }

    public void unregisterWorker(ServerLink serverLink) {
        SuperIO.writeTextToFile("/tmp/masterworker_worker_count", "" + (workerPool.getSize()));
        log.debug("unregisterWorker: " + serverLink);
//        jobPool.requeueJobIfRunning(serverLink);
        MessageSender.send(new KillMessage(), serverLink, new MessageSender.FailureHandler() {
            public void onFailure(ServerLink client) {
                log.debug("unregisterWorker: IOException while sending KillMessage to worker - removing worker");
            }
        }, "unregisterWorker client("+ serverLink.getLinkID() +")");
        workerPool.removeWorker(serverLink);
        notifyObservers();
    }

    public void updateWorkerHealth(HealthMessage healthMessage, ServerLink serverLink) {
        //log.debug("updateWorkerHealth: " + serverLink);
        workerPool.updateWorkerHealth(healthMessage.getSystemLoad(), healthMessage.getVmMemoryUsage(), healthMessage.getDiskUsages(), serverLink);
        runJobIfNecessaryAndPossible();
        notifyObservers();
    }

    public void updateJobProgress(JobProgressMessage jobProgressMessage) {
        log.debug("updateJobProgress: " + jobProgressMessage);
        jobPool.updateJobProgress(jobProgressMessage.getJobID(), jobProgressMessage.getProgress());
        notifyObservers();
    }



    public void setRunMethodRemoteResultMessage(RunMethodRemoteResultMessage runMethodRemoteResultMessage) {
        log.debug("setRunMethodRemoteResultMessage: " + runMethodRemoteResultMessage.getMethodID());
        jobPool.setRunMethodRemoteResultMessage(runMethodRemoteResultMessage);
        notifyObservers();
    }

    //TODO: Move this to the SuperIO
    private static HashMap<String, AtomicLong> counterStatusMap = new HashMap<>();
    public static void increaseCounterStatus(String filename){
        AtomicLong counter = counterStatusMap.get(filename);
        if(counter == null){
            try{
                if(new File(filename).exists()) {
                    counter = new AtomicLong(new Long(SuperIO.readTextFromFile(filename)));
                }
            } catch (Exception e){
                counter = new AtomicLong(0L);
            }
            counterStatusMap.put(filename, counter);
        }
        final long increment = counter.getAndIncrement();
        if(increment % 50 == 0){
            SuperIO.writeTextToFile(filename, ""+ increment);
        }

    }


    public void setResult(JobResultMessage result, ServerLink serverLink) {
        increaseCounterStatus("/tmp/masterworker_result_jobs_count");
        log.debug("setResult["+ (result != null ? result.getJobID() : "NULL") +"]: " + serverLink);
        jobPool.setResult(result);
        workerPool.setIdle(true, serverLink);
        WorkerPool.WorkerEntry workerEntry = workerPool.getWorkerEntry(serverLink);
        workerEntry.jobReturnedStats();
        runJobIfNecessaryAndPossible();
    }


    // Observer services
    public void registerObserver(ServerLink serverLink) {
        log.debug("registerObserver: " + serverLink);
        observers.add(serverLink);
        //notifyObservers();
    }



    public void unregisterObserver(ServerLink serverLink) {
        log.debug("unregisterObserver: " + serverLink);
        observers.remove(serverLink);
    }


    long lastUpdate = System.currentTimeMillis();
    void notifyObservers() {
//        log.debug("notifyObservers.size("+ observers.size() +"): ");
        if (observers == null || observers.isEmpty()) return;
        long now = System.currentTimeMillis();
        if(now - lastUpdate < 1000 * 5){
            return;
        }
        lastUpdate = now;
        UpdateMessage updateMessage = new UpdateMessage();
        updateMessage.setObserverJobMessages(jobPool.getObserverJobMessageList());
        updateMessage.setObserverWorkerMessages(workerPool.getObserverWorkerMessageList());
        updateMessage.setStarted(started);
        for (final ServerLink observer: (Set<ServerLink>) observers.clone()) {
            MessageSender.sendOrTimeout(updateMessage, observer, new MessageSender.FailureHandler() {
                public void onFailure(ServerLink client) {
                    log.warn("Failed to send message to observer "+ observer.getOtherHostPort() + " - removing observer");
                    unregisterObserver(observer);
                }
            });
        }
    }


    ThreadPoolExecutor clientExecutor = new ThreadPoolExecutor(20, 5000, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

    public void acceptClientConnection(ServerSocket serverSocket) {
//        log.debug("acceptClientConnection[1]: " + serverSocket);
        MasterServer.increaseCounterStatus("/tmp/masterworker_client_connection_count");
        ServerLink serverLink = acceptConnection(serverSocket);
        if (serverLink != null) {
            //TODO: Read 5 mins if the queue is working with this or not ...
            log.debug("acceptClientConnection[1.1]: clientExecutor.getActiveCount(" + clientExecutor.getActiveCount() + "), clientExecutor.getPoolSize(" + clientExecutor.getPoolSize() + "), clientExecutor.getQueue().size(" + clientExecutor.getQueue().size() + ")");
            clientExecutor.submit(new MasterClientThread(this, serverLink));
            if(clientExecutor.getActiveCount() == clientExecutor.getPoolSize()){
                clientExecutor.setCorePoolSize(clientExecutor.getCorePoolSize() + 1);
            }

        }
//        log.debug("acceptClientConnection[2]: " + serverSocket);
        notifyObservers();
//        log.debug("acceptClientConnection[3]: " + serverSocket);
    }

    ThreadPoolExecutor workerExecutor = new ThreadPoolExecutor(20, 5000, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

    public void acceptWorkerConnection(ServerSocket serverSocket) {
//        log.debug("acceptWorkerConnection: " + serverSocket);
        ServerLink serverLink = acceptConnection(serverSocket);
        if (serverLink != null) {
            log.debug("acceptWorkerConnection[1.1]: workerExecutor.getActiveCount(" + clientExecutor.getActiveCount() + "), workerExecutor.getPoolSize(" + clientExecutor.getPoolSize() + "), workerExecutor.getQueue().size(" + clientExecutor.getQueue().size() + ")");
            workerExecutor.submit(new MasterWorkerThread(this, serverLink));
            if(workerExecutor.getActiveCount() == workerExecutor.getPoolSize()){
                workerExecutor.setCorePoolSize(workerExecutor.getCorePoolSize() + 1);
            }
        }
        notifyObservers();
    }

    public void acceptObserverConnection(ServerSocket serverSocket) {
//        log.debug("acceptObserverConnection: " + serverSocket);
        ServerLink serverLink = acceptConnection(serverSocket);
        if (serverLink != null) {
            new MasterObserverThread(this, serverLink).start();
        }
    }

    private ServerLink acceptConnection(ServerSocket serverSocket) {
//        log.debug("acceptConnection: " + serverSocket);
        try {
            Socket socket;
            socket = serverSocket.accept();
            socket.setKeepAlive(true);
            socket.setSoTimeout(1000 * 180);
            socket.setTcpNoDelay(true);
            return new ServerLink(socket);
        } catch (Exception e) {
            log.warn("Socket could not be accepted", e);
            return null;
        }
    }

//    static long masterworker_input_jobs_count = new File("/tmp/masterworker_input_jobs_count").exists() ? new Long(SuperIO.readTextFromFile("/tmp/masterworker_input_jobs_count")) : 0;

    private long lastPrint = 0;
    protected void printStatus() {
        try {
            lastPrint = System.currentTimeMillis();
            StringBuilder builder = new StringBuilder();
            builder.append("---------------------------------- Master Status ---------------------------------- START\n");
            String jobPoolStr = jobPool.toString();
            String workPoolStr = workerPool.toString();
            builder.append(jobPoolStr + workPoolStr);
            builder.append("\n");
            builder.append("---------------------------------- Master Status ---------------------------------- ENDS");
            final String content = builder.toString();
            if(DEBUG){
                log.debug(jobPoolStr);
                log.debug(workPoolStr);
                log.debug(content);
            }
            SuperIO.writeTextToFile("/tmp/master-status", content);
            SuperIO.writeTextToFile("/tmp/master-status-workpool", workPoolStr);
            SuperIO.writeTextToFile("/tmp/master-status-jobpool", jobPoolStr);
        } catch (Exception e){
            log.error("Some error when printStatus:" + e, e);
        }
    }

    private void runJobIfNecessaryAndPossible() {
        try {
            lock.lock();
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    protected final Lock lock = new ReentrantLock();
    protected final Condition condition = lock.newCondition();

    protected void runJobs() {
        try {
            long a = System.currentTimeMillis();
            JobPool.JobEntry jobEntry = null;
            WorkerPool.WorkerEntry workerEntry = null;

            jobEntry = jobPool.firstJob();
            if (jobEntry == null) {
                log.debug("runJobs: No Job in queue to run :-D ");
                try {
                    lock.lock();
                    condition.await(10, TimeUnit.SECONDS);
                } finally {
                    lock.unlock();
                }
                return;
            } else {
                if(jobEntry.jobMessage.getDealline() > 0 && jobEntry.jobMessage.getDealline() < System.currentTimeMillis()){
                    log.warn("Job was timeout before starting ... Will remove it from queue: " + jobEntry.jobMessage.getJobID());
                    return;
                }
            }
            
            //WORKER
            //2: DownloadExecutor - Occupied
            //3: NlpExecutor
            //4: NlpExecutor
            //5: NlpExecutor
            //6: ToUpperCaseExecutor
            
            // JOB's
            //1: DownloadJob
            //200: DownloadJob
            //203: NlpJob
            //204: NlpJob
            //205: NlpJob
            //206: ToUpperCaseJob
            
            workerEntry = workerPool.getBestWorker(jobEntry.jobMessage.getExecutorClassName());
            long b = System.currentTimeMillis();
            if (workerEntry == null) {
                log.debug("runJobs: No available worker to run job - to run the first job... We will look in the queue for other jobs("+ jobPool.getQueueSize() +")");
                List<JobPool.JobEntry> diffJobs = jobPool.getDiffJobs(jobEntry.jobMessage.getExecutorClassName());
                for (int i = 0; workerEntry == null && i < diffJobs.size(); i++) {
                    workerEntry = workerPool.getBestWorker(diffJobs.get(i).jobMessage.getExecutorClassName());
                    jobEntry = diffJobs.get(i);
                }
                if (workerEntry == null) {
                    try {
                        lock.lock();
                        condition.await(10, TimeUnit.SECONDS);
                    } finally {
                        lock.unlock();
                    }
                    return;
                }
            }
            long c = System.currentTimeMillis();

             MasterServer.increaseCounterStatus("/tmp/masterworker_input_jobs_count");
            log.debug("runJobs: Found worker to run job: " + workerEntry);
            jobPool.jobTaken(jobEntry, workerEntry.serverLink);
            workerEntry.jobTakenStats();
            workerPool.setIdle(false, workerEntry);
            long d = System.currentTimeMillis();
            WorkerPool.WorkerEntry finalWorkerEntry = workerEntry;
            MessageSender.send(jobEntry.jobMessage, workerEntry.serverLink, new MessageSender.FailureHandler() {
                public void onFailure(ServerLink client) {
                    log.debug("IOException while sending job to worker - removing worker - will run unregisterWorker("+ client.getLinkID() +") ");
                    MasterServer.this.unregisterWorker(finalWorkerEntry.serverLink);
                }
            }, "sendWork("+ jobEntry.getJobID() +") to WorkerLink("+ workerEntry.serverLink.getLinkID() +")");
            long e = System.currentTimeMillis();
            log.debug("runJobs: TimeStats("+ (e-a) +") 1("+ (b-a) +") 2("+ (c-b) +") 3("+ (d-c) +") 4("+ (e-d)+")");
        } catch (Exception e){
            log.error("Some error when running runJobIfNecessaryAndPossible:" + e, e);
        }
    }



    public void kill(String jobID) {
        final JobPool.JobEntry jobEntry = jobPool.getJobEntry(jobID);
        if (jobEntry != null) {
            log.warn("kill job[" + jobID + "]: " + jobEntry);
            if (jobEntry.worker != null && jobEntry.getStatus() == JobStatus.IN_PROGRESS) {
                MessageSender.send(new KillMessage(), jobEntry.worker, new MessageSender.FailureHandler() {
                    public void onFailure(ServerLink worker) {
                        log.debug("IOException while sending StopMessage to worker - removing worker("+ jobEntry.worker.getLinkID() +")");
                        unregisterWorker(worker);
                    }
                }, "Sending KILL to jobEntry.worker("+ jobEntry.worker.getLinkID() +")");
                workerPool.removeWorker(jobEntry.worker);
            }
        }
        jobPool.kill(jobID);
    }

    public void cancelJob(String jobID) {
        final JobPool.JobEntry jobEntry = jobPool.getJobEntry(jobID);
        if (jobEntry != null) {
            log.warn("kill job[" + jobID + "]: " + jobEntry);
            if (jobEntry.worker != null && jobEntry.getStatus() == JobStatus.IN_PROGRESS) {
                MessageSender.send(new CancelJobMessage(jobID), jobEntry.worker, new MessageSender.FailureHandler() {
                    public void onFailure(ServerLink worker) {
                        log.debug("IOException while sending StopMessage to worker - removing worker("+ jobEntry.worker.getLinkID() +")");
                        unregisterWorker(worker);
                    }
                }, "Sending KILL to jobEntry.worker("+ jobEntry.worker.getLinkID() +")");
            }
        }
        jobPool.kill(jobID);
    }

    public void pingAllWorkers() {
        for(ServerLink worker : workerPool.getAllServerLinksFromWorkers()){
            MessageSender.send(new PingMessage(), worker, new MessageSender.FailureHandler() {
                public void onFailure(ServerLink worker) {
                    log.debug("IOException while sending PingMessage to worker - removing worker("+ worker +")");
                    unregisterWorker(worker);
                }
            }, "Sending Ping to jobEntry.worker("+ worker.getLinkID() +")");

        }
    }

    public void sendAllHealthMessages() {
        for(ServerLink worker : workerPool.getAllServerLinksFromWorkers()){
            MessageSender.send(new HealthMessageRequest(), worker, new MessageSender.FailureHandler() {
                public void onFailure(ServerLink worker) {
                    log.debug("IOException while sending HealthMessageRequest to worker - removing worker("+ worker +")");
                    unregisterWorker(worker);
                }
            }, "Sending HealthMessageRequest to jobEntry.worker("+ worker.getLinkID() +")");

        }
    }

    public void pingAllClients() {
        final Set<ServerLink> allClients = jobPool.getAllClients();
        for(ServerLink client : allClients){
            MessageSender.send(new PingMessage(), client, new MessageSender.FailureHandler() {
                public void onFailure(ServerLink worker) {
                    log.debug("IOException while sending PingMessage to client - removing client("+ worker +")");
                }
            }, "Sending Ping to jobEntry.client("+ client.getLinkID() +")");

        }
    }
}
