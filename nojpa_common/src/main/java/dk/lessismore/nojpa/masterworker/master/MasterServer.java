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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
    private JobPool jobPool = new JobPool();
    private WorkerPool workerPool = new WorkerPool();
    private HashSet<ServerLink> observers = new HashSet<ServerLink>();
    private final Serializer storeSerializer = new XmlSerializer();
    private final Calendar started = Calendar.getInstance();


    public void runMethodRemote(RunMethodRemoteBeanMessage runMethodRemoteBeanMessage, ServerLink serverLink) throws IOException {
        jobPool.runMethodRemote(runMethodRemoteBeanMessage, serverLink);
        notifyObservers();
    }



    // Client services
    synchronized void startListen(String jobID, ServerLink client) {
        if (jobID == null) log.error("Can't listen to null job");
        else {
            boolean listenerAdded = jobPool.addListener(jobID, client);
            // No job entry in pool, look for stored results.
            if (! listenerAdded) {
                log.debug("Trying to find stored job result");
                JobResultMessage jobResultMessage = restoreResult(jobID);
                if (jobResultMessage == null) {
                    log.debug("No stored result found, sending back exception");
                    jobResultMessage = new JobResultMessage(jobID);
                    jobResultMessage.setMasterException(new JobDoesNotExistException());
                }
                MessageSender.sendResultToClient(jobResultMessage, client, new MessageSender.FailureHandler() {
                    public void onFailure(ServerLink client) {
                        log.warn("Failed to send restored result to client");
                    }
                });
            }
        }
        notifyObservers();
    }

    synchronized void stopListen(ServerLink client) {
        log.debug("stopListen: " + client);
        jobPool.removeListener(client);
        notifyObservers();
    }

    synchronized public void queueJob(JobMessage jobMessage) {
        SuperIO.writeTextToFile("/tmp/masterworker_queue_size", "" + (jobPool.getQueueSize()));
        log.debug("queueJob("+ jobPool.getQueueSize() +"): " + jobMessage);
        jobPool.addJob(jobMessage);
        runJobIfNecessaryAndPossible();
        notifyObservers();
    }


    // Worker services
    synchronized public void registerWorker(RegistrationMessage registrationMessage , ServerLink serverLink) {
        SuperIO.writeTextToFile("/tmp/masterworker_worker_count", "" + (workerPool.getSize()));
        log.debug("registerWorker: " + serverLink);
        workerPool.addWorker(registrationMessage, serverLink);
        runJobIfNecessaryAndPossible();
        notifyObservers();
    }

    synchronized public void unregisterWorker(ServerLink serverLink) {
        SuperIO.writeTextToFile("/tmp/masterworker_worker_count", "" + (workerPool.getSize()));
        log.debug("unregisterWorker: " + serverLink);
        jobPool.requeueJobIfRunning(serverLink);
        workerPool.removeWorker(serverLink);
        notifyObservers();
    }

    synchronized public void updateWorkerHealth(HealthMessage healthMessage, ServerLink serverLink) {
        //log.debug("updateWorkerHealth: " + serverLink);
        boolean applicableBefore = workerPool.applicable(serverLink);
        workerPool.updateWorkerHealth(healthMessage.getSystemLoad(), healthMessage.getVmMemoryUsage(),
                healthMessage.getDiskUsages(), serverLink);
        if (!applicableBefore) {
            boolean applicableAfter = workerPool.applicable(serverLink);
            if (applicableAfter) runJobIfNecessaryAndPossible();
        }
        //notifyObservers();
    }

    synchronized public void updateJobProgress(JobProgressMessage jobProgressMessage) {
        log.debug("updateJobProgress: " + jobProgressMessage);
        jobPool.updateJobProgress(jobProgressMessage.getJobID(), jobProgressMessage.getProgress());
        notifyObservers();
    }



    synchronized public void setRunMethodRemoteResultMessage(RunMethodRemoteResultMessage runMethodRemoteResultMessage) {
        //storeResult(result); TODO
        log.debug("setRunMethodRemoteResultMessage: " + runMethodRemoteResultMessage);
        jobPool.setRunMethodRemoteResultMessage(runMethodRemoteResultMessage);
        notifyObservers();
    }

    static long masterworker_result_jobs_count = new File("/tmp/masterworker_result_jobs_count").exists() ? new Long(SuperIO.readTextFromFile("/tmp/masterworker_result_jobs_count")) : 0;

    synchronized public void setResult(JobResultMessage result, ServerLink serverLink) {
        SuperIO.writeTextToFile("/tmp/masterworker_result_jobs_count", "" + (masterworker_result_jobs_count++));

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

    static long masterworker_client_connection_count = new File("/tmp/masterworker_client_connection_count").exists() ? new Long(SuperIO.readTextFromFile("/tmp/masterworker_client_connection_count")) : 0;

    public void acceptClientConnection(ServerSocket serverSocket) {
        SuperIO.writeTextToFile("/tmp/masterworker_client_connection_count", "" + (masterworker_client_connection_count++));
        log.debug("acceptClientConnection[1]: " + serverSocket);
        ServerLink serverLink = acceptConnection(serverSocket);
        if (serverLink != null) {
            new MasterClientThread(this, serverLink).start();
        }
        log.debug("acceptClientConnection[2]: " + serverSocket);
        notifyObservers();
        log.debug("acceptClientConnection[3]: " + serverSocket);
    }

    public void acceptWorkerConnection(ServerSocket serverSocket) {
        log.debug("acceptWorkerConnection: " + serverSocket);
        ServerLink serverLink = acceptConnection(serverSocket);
        if (serverLink != null) {
            new MasterWorkerThread(this, serverLink).start();
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
        log.debug("storeResult: " + result);
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

    static long masterworker_input_jobs_count = new File("/tmp/masterworker_input_jobs_count").exists() ? new Long(SuperIO.readTextFromFile("/tmp/masterworker_input_jobs_count")) : 0;

    synchronized private void runJobIfNecessaryAndPossible() {
        log.debug("runJobIfNecessaryAndPossible jobPool("+ jobPool.getQueueSize() +")");
        System.out.println("---------------------------------- Master Status ---------------------------------- START");
        System.out.println(jobPool.toString() + workerPool.toString());
        System.out.println("---------------------------------- Master Status ---------------------------------- ENDS");
        final JobPool.JobEntry jobEntry = jobPool.firstJob();
        if (jobEntry == null) {
            log.debug("No Job in queue to run");
            return;
        }
        SuperIO.writeTextToFile("/tmp/masterworker_input_jobs_count", "" + (masterworker_input_jobs_count++));
        final WorkerPool.WorkerEntry workerEntry = workerPool.getBestApplicableWorker(
                jobEntry.jobMessage.getExecutorClassName());
        if (workerEntry == null) {
            log.debug("No available worker to run job");
            return;
        }
        log.debug("Found worker to run job: "+ workerEntry);
        jobPool.jobTaken(jobEntry, workerEntry.serverLink);
        workerEntry.jobTakenStats();


        MessageSender.send(jobEntry.jobMessage, workerEntry.serverLink, new MessageSender.FailureHandler() {
            public void onFailure(ServerLink client) {
                log.debug("IOException while sending job to worker - removing worker");
                MasterServer.this.unregisterWorker(workerEntry.serverLink);
            }
        });
        notifyObservers();
    }



    public void kill(String jobID) {
        JobPool.JobEntry jobEntry = jobPool.getJobEntry(jobID);
        log.debug("kill job[" + jobID + "]: " + jobEntry);
        if (jobEntry != null) {
            if (jobEntry.worker != null && jobEntry.getStatus() == JobStatus.IN_PROGRESS) {
                MessageSender.send(new KillMessage(), jobEntry.worker, new MessageSender.FailureHandler() {
                    public void onFailure(ServerLink client) {
                        log.debug("IOException while sending KillMessage to worker - removing worker");
                    }
                });
                workerPool.removeWorker(jobEntry.worker);
            }
            jobPool.kill(jobID);
        }
    }

    public void restartAllWorkers() {
        log.debug("restartAllWorkers");
        Map.Entry<ServerLink, WorkerPool.WorkerEntry>[] entries = workerPool.pool.entrySet().toArray(new Map.Entry[workerPool.pool.size()]);
        for(int i = 0; i < entries.length; i++){
            log.debug("restartAllWorkers("+ i +"/"+ entries.length +")");
            Map.Entry<ServerLink, WorkerPool.WorkerEntry> entry = entries[i];
            try {
                entry.getKey().stopPinger();
                entry.getKey().write(new KillMessage());
            } catch (IOException e) {
                log.warn("When restartAllWorkers we got from worker("+ entry.getValue().toString() +")  : "+ e, e);
            }
        }

    }
}
