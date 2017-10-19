package dk.lessismore.nojpa.masterworker.master;

import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.exceptions.JobDoesNotExistException;
import dk.lessismore.nojpa.masterworker.messages.HealthMessage;
import dk.lessismore.nojpa.masterworker.messages.JobMessage;
import dk.lessismore.nojpa.masterworker.messages.JobProgressMessage;
import dk.lessismore.nojpa.masterworker.messages.JobResultMessage;
import dk.lessismore.nojpa.masterworker.messages.KillMessage;
import dk.lessismore.nojpa.masterworker.messages.RegistrationMessage;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteBeanMessage;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteResultMessage;
import dk.lessismore.nojpa.masterworker.messages.observer.UpdateMessage;
import dk.lessismore.nojpa.net.link.ServerLink;
import dk.lessismore.nojpa.properties.PropertiesListener;
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
    static {
        properties.addListener(new PropertiesListener() {
            public void onChanged() {
                log.info("Master.properties changed");
            }
        });
    }

    private static boolean DEBUG = true;

    private final JobPool jobPool = new JobPool();
    private final WorkerPool workerPool = new WorkerPool();
    private final HashSet<ServerLink> observers = new HashSet<ServerLink>();
    private final Serializer storeSerializer = new XmlSerializer();
    private final Calendar started = Calendar.getInstance();


    public void runMethodRemote(RunMethodRemoteBeanMessage runMethodRemoteBeanMessage, ServerLink serverLink) throws IOException {
        jobPool.runMethodRemote(runMethodRemoteBeanMessage, serverLink);
        notifyObservers();
    }



    // Client services
    void startListen(String jobID, ServerLink client) {
        if (jobID == null) log.error("Can't listen to null job");
        else {
            boolean listenerAdded = jobPool.addListener(jobID, client);
            // No job entry in pool, look for stored results.
            if (! listenerAdded) {
                log.debug("Trying to find stored job result. jobID("+ jobID +")");
                JobResultMessage jobResultMessage = restoreResult(jobID);
                if (jobResultMessage == null) {
                    log.error("No stored result found, sending back exception for jobID("+ jobID +")");
                    jobResultMessage = new JobResultMessage(jobID);
                    jobResultMessage.setMasterException(new JobDoesNotExistException());
                } else {
                    MessageSender.sendResultToClient(jobResultMessage, client, new MessageSender.FailureHandler() {
                        public void onFailure(ServerLink client) {
                            log.warn("Failed to send restored result to client");
                        }
                    });
                }
            }
        }
        notifyObservers();
    }

    void stopListen(ServerLink client) {
        log.debug("stopListen: " + client);
        jobPool.removeListener(client);
        notifyObservers();
    }

    public void queueJob(JobMessage jobMessage) {
        MasterServer.increaseCounterStatus("/tmp/masterworker_queue_size");
        log.debug("queueJob("+ jobPool.getQueueSize() +"): " + jobMessage);
        jobPool.addJob(jobMessage);
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
        runJobIfNecessaryAndPossible();
        notifyObservers();
    }

    public void updateWorkerHealth(HealthMessage healthMessage, ServerLink serverLink) {
        //log.debug("updateWorkerHealth: " + serverLink);
        boolean ableToWorkAfter = false;
        boolean ableToWorkBefore = workerPool.applicable(serverLink);
        workerPool.updateWorkerHealth(healthMessage.getSystemLoad(), healthMessage.getVmMemoryUsage(), healthMessage.getDiskUsages(), serverLink);
        if (!ableToWorkBefore) {
            ableToWorkAfter = workerPool.applicable(serverLink);
        }
        runJobIfNecessaryAndPossible();
        //notifyObservers();
    }

    public void updateJobProgress(JobProgressMessage jobProgressMessage) {
        log.debug("updateJobProgress: " + jobProgressMessage);
        jobPool.updateJobProgress(jobProgressMessage.getJobID(), jobProgressMessage.getProgress());
        notifyObservers();
    }



    public void setRunMethodRemoteResultMessage(RunMethodRemoteResultMessage runMethodRemoteResultMessage) {
        //storeResult(result); TODO
        log.debug("setRunMethodRemoteResultMessage: " + runMethodRemoteResultMessage.getMethodID());
        jobPool.setRunMethodRemoteResultMessage(runMethodRemoteResultMessage);
        notifyObservers();
    }

    private static HashMap<String, AtomicLong> counterStatusMap = new HashMap<>();
    public static void increaseCounterStatus(String filename){
        AtomicLong counter = counterStatusMap.get(filename);
        if(counter == null){
            try{
                counter = new AtomicLong(new Long(SuperIO.readTextFromFile(filename)));
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
        storeResult(result);
        jobPool.setResult(result);
        workerPool.setIdle(true, serverLink);
        WorkerPool.WorkerEntry workerEntry = workerPool.getWorkerEntry(serverLink);
        workerEntry.jobReturnedStats();
        runJobIfNecessaryAndPossible();
    }


    // Observer services
    public void registerObserver(ServerLink serverLink) {
        log.debug("registerObserver: " + serverLink);
        addObserver(serverLink);
        //notifyObservers();
    }



    // Local stuff
    private void addObserver(ServerLink serverLink) {
        log.debug("addObserver: " + serverLink);
        observers.add(serverLink);
    }

    private void removeObserver(ServerLink serverLink) {
        log.debug("removeObserver: " + serverLink);
        observers.remove(serverLink);
    }


    long lastUpdate = System.currentTimeMillis();
    void notifyObservers() {
        log.debug("notifyObservers.size("+ observers.size() +"): ");
        long now = System.currentTimeMillis();
        if(now - lastUpdate < 1000 * 1){
            return;
        }
        lastUpdate = now;
        if (observers == null || observers.isEmpty()) return;
        UpdateMessage updateMessage = new UpdateMessage();
        updateMessage.setObserverJobMessages(jobPool.getObserverJobMessageList());
        updateMessage.setObserverWorkerMessages(workerPool.getObserverWorkerMessageList());
        updateMessage.setStarted(started);
        final List<ServerLink> deadObservers = new ArrayList<ServerLink>();
        for (final ServerLink observer: (Set<ServerLink>) observers.clone()) {
            MessageSender.sendOrTimeout(updateMessage, observer, new MessageSender.FailureHandler() {
                public void onFailure(ServerLink client) {
                    log.warn("Failed to send message to observer "+ observer.getOtherHostPort() + " - removing observer");
                    deadObservers.add(observer);
                }
            });
        }
        if (!deadObservers.isEmpty()) {
            for (ServerLink deadObserver: deadObservers) {
                removeObserver(deadObserver);
            }
            deadObservers.clear();
        }
    }


    ThreadPoolExecutor clientExecutor = new ThreadPoolExecutor(20, 5000, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

    public void acceptClientConnection(ServerSocket serverSocket) {
        log.debug("acceptClientConnection[1]: " + serverSocket);
        MasterServer.increaseCounterStatus("/tmp/masterworker_client_connection_count");
        ServerLink serverLink = acceptConnection(serverSocket);
        if (serverLink != null) {
            log.debug("acceptClientConnection[1.1]: clientExecutor.getActiveCount(" + clientExecutor.getActiveCount() + "), clientExecutor.getPoolSize(" + clientExecutor.getPoolSize() + "), clientExecutor.getQueue().size(" + clientExecutor.getQueue().size() + ")");
            clientExecutor.submit(new MasterClientThread(this, serverLink));
            if(clientExecutor.getActiveCount() == clientExecutor.getPoolSize()){
                clientExecutor.setCorePoolSize(clientExecutor.getCorePoolSize() + 1);
            }

        }
        log.debug("acceptClientConnection[2]: " + serverSocket);
        notifyObservers();
        log.debug("acceptClientConnection[3]: " + serverSocket);
    }

    ThreadPoolExecutor workerExecutor = new ThreadPoolExecutor(20, 5000, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>());

    public void acceptWorkerConnection(ServerSocket serverSocket) {
        log.debug("acceptWorkerConnection: " + serverSocket);
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
        log.debug("acceptObserverConnection: " + serverSocket);
        ServerLink serverLink = acceptConnection(serverSocket);
        if (serverLink != null) {
            new MasterObserverThread(this, serverLink).start();
        }
    }

    private ServerLink acceptConnection(ServerSocket serverSocket) {
        log.debug("acceptConnection: " + serverSocket);
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

    protected static File getStoredResultFile(String jobID) {
        log.debug("getStoredResultFile: " + jobID);
        String resultDirName = properties.getStoreResultDir();
        File resultDir = new File(resultDirName);
        if (! resultDir.isDirectory()) {
            boolean success = resultDir.mkdirs();
            if (! success) {
                log.error("Failed to create directory to store results at "+ resultDirName);
                return null;
            }
        }
        String resultFileName = jobID + ".xml";
        return new File(resultDir, resultFileName);
    }

    private void storeResult(JobResultMessage result) {
        log.debug("storeResult: " + result.getJobID());
        File resultFile = getStoredResultFile(result.getJobID());
        if(!resultFile.getParentFile().exists()){
            resultFile.getParentFile().mkdirs();
        }
        if (resultFile == null) return;
        try {
            storeSerializer.store(result, resultFile);
            log.debug("Result saved to file " + resultFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed save result to file " + resultFile.getAbsolutePath(), e);
        }
    }

    private JobResultMessage restoreResult(String jobID) {
        log.debug("restoreResult: " + jobID);
        File resultFile = getStoredResultFile(jobID);
        if (resultFile == null) return null;
        try {
            return (JobResultMessage) storeSerializer.restore(resultFile);
        } catch (IOException e) {
            log.error("Error while trying to load stored result");
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
            }
            workerEntry = workerPool.getBestApplicableWorker(jobEntry.jobMessage.getExecutorClassName());
            long b = System.currentTimeMillis();
            if (workerEntry == null) {
                log.debug("runJobs: No available worker to run job - to run the first job... We will look in the queue for other jobs");
                List<JobPool.JobEntry> diffJobs = jobPool.getDiffJobs(jobEntry.jobMessage.getExecutorClassName());
                for (int i = 0; workerEntry == null && i < diffJobs.size(); i++) {
                    workerEntry = workerPool.getBestApplicableWorker(diffJobs.get(i).jobMessage.getExecutorClassName());
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
            long d = System.currentTimeMillis();
            WorkerPool.WorkerEntry finalWorkerEntry = workerEntry;
            MessageSender.send(jobEntry.jobMessage, workerEntry.serverLink, new MessageSender.FailureHandler() {
                public void onFailure(ServerLink client) {
                    log.debug("IOException while sending job to worker - removing worker - will run unregisterWorker("+ client.getLinkID() +")");
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
        log.warn("kill job[" + jobID + "]: " + jobEntry);
        if (jobEntry != null) {
            if (jobEntry.worker != null && jobEntry.getStatus() == JobStatus.IN_PROGRESS) {
                MessageSender.send(new KillMessage(), jobEntry.worker, new MessageSender.FailureHandler() {
                    public void onFailure(ServerLink client) {
                        log.debug("IOException while sending KillMessage to worker - removing worker("+ jobEntry.worker.getLinkID() +")");
                    }
                }, "Sending KILL to jobEntry.worker("+ jobEntry.worker.getLinkID() +")");
                workerPool.removeWorker(jobEntry.worker);
            }
            jobPool.kill(jobID);
        }
    }

    public void restartAllWorkers() {
        try {
            log.debug("restartAllWorkers");
            Map.Entry<ServerLink, WorkerPool.WorkerEntry>[] entries = workerPool.pool.entrySet().toArray(new Map.Entry[workerPool.pool.size()]);
            for (int i = 0; i < entries.length; i++) {
                log.debug("restartAllWorkers(" + i + "/" + entries.length + ")");
                Map.Entry<ServerLink, WorkerPool.WorkerEntry> entry = entries[i];
                try {
                    entry.getKey().stopPinger();
                    entry.getKey().write(new KillMessage());
                } catch (IOException e) {
                    log.warn("When restartAllWorkers we got from worker(" + entry.getValue().toString() + ")  : " + e, e);
                }
            }
        } catch (Exception e){
            System.exit(-1); //TODO: Remove this in the future, when it is running stable
        }

    }
}
