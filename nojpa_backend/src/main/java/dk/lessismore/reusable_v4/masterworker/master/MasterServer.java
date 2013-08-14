package dk.lessismore.reusable_v4.masterworker.master;

import dk.lessismore.reusable_v4.masterworker.exceptions.JobDoesNotExistException;
import dk.lessismore.reusable_v4.masterworker.messages.*;
import dk.lessismore.reusable_v4.masterworker.messages.observer.UpdateMessage;
import dk.lessismore.reusable_v4.net.link.ServerLink;
import dk.lessismore.reusable_v4.properties.PropertiesListener;
import dk.lessismore.reusable_v4.properties.PropertiesProxy;
import dk.lessismore.reusable_v4.serialization.Serializer;
import dk.lessismore.reusable_v4.serialization.XmlSerializer;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class MasterServer {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MasterServer.class);
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
        //notifyObservers();
    }

    synchronized void stopListen(ServerLink client) {
        jobPool.removeListener(client);
        //notifyObservers();
    }

    synchronized public void queueJob(JobMessage jobMessage) {
        jobPool.addJob(jobMessage);
        runJobIfNecessaryAndPossible();
        //notifyObservers();
    }


    // Worker services
    synchronized public void registerWorker(String[] knownClasses, ServerLink serverLink) {
        workerPool.addWorker(knownClasses, serverLink);
        runJobIfNecessaryAndPossible();
    }

    synchronized public void unregisterWorker(ServerLink serverLink) {
        jobPool.requeueJobIfRunning(serverLink);
        workerPool.removeWorker(serverLink);
    }

    synchronized public void updateWorkerHealth(HealthMessage healthMessage, ServerLink serverLink) {
        boolean applicableBefore = workerPool.applicable(serverLink);
        workerPool.updateWorkerHealth(healthMessage.getSystemLoad(), healthMessage.getVmMemoryUsage(),
                healthMessage.getDiskUsages(), serverLink);
        if (!applicableBefore) {
            boolean applicableAfter = workerPool.applicable(serverLink);
            if (applicableAfter) runJobIfNecessaryAndPossible();
        }
    }

    synchronized public void updateJobProgress(JobProgressMessage jobProgressMessage) {
        jobPool.updateJobProgress(jobProgressMessage.getJobID(), jobProgressMessage.getProgress());
        //notifyObservers();
    }



    synchronized public void setRunMethodRemoteResultMessage(RunMethodRemoteResultMessage runMethodRemoteResultMessage) {
        //storeResult(result); TODO
        jobPool.setRunMethodRemoteResultMessage(runMethodRemoteResultMessage);
        //notifyObservers();
    }

    synchronized public void setResult(JobResultMessage result) {
        storeResult(result);
        jobPool.setResult(result);
        //notifyObservers();
    }


    // Observer services
    public void registerObserver(ServerLink serverLink) {
        addObserver(serverLink);
        //notifyObservers();
    }



    // Local stuff
    private void addObserver(ServerLink serverLink) {
        observers.add(serverLink);
    }

    private void removeObserver(ServerLink serverLink) {
        observers.remove(serverLink);
    }


    void notifyObservers() {
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

    public void acceptClientConnection(ServerSocket serverSocket) {
        ServerLink serverLink = acceptConnection(serverSocket);
        if (serverLink != null) {
            new MasterClientThread(this, serverLink).start();
        }
    }

    public void acceptWorkerConnection(ServerSocket serverSocket) {
        ServerLink serverLink = acceptConnection(serverSocket);
        if (serverLink != null) {
            new MasterWorkerThread(this, serverLink).start();
        }
    }

    public void acceptObserverConnection(ServerSocket serverSocket) {
        ServerLink serverLink = acceptConnection(serverSocket);
        if (serverLink != null) {
            new MasterObserverThread(this, serverLink).start();
        }
    }

    private ServerLink acceptConnection(ServerSocket serverSocket) {
        try {
            Socket socket;
            socket = serverSocket.accept();
            socket.setKeepAlive(true);
            socket.setSoTimeout(1000 * 180);
            socket.setTcpNoDelay(true);
            return new ServerLink(socket);
        } catch (IOException e) {
            log.warn("Socket could not be accepted", e);
            return null;
        }
    }

    private File getStoredResultFile(String jobID) {
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
        File resultFile = getStoredResultFile(result.getJobID());
        if (resultFile == null) return;
        try {
            storeSerializer.store(result, resultFile);
            log.debug("Result saved to file " + resultFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed save result to file " + resultFile.getAbsolutePath(), e);
        }
    }

    private JobResultMessage restoreResult(String jobID) {
        File resultFile = getStoredResultFile(jobID);
        if (resultFile == null) return null;
        try {
            return (JobResultMessage) storeSerializer.restore(resultFile);
        } catch (IOException e) {
            log.error("Error while trying to load stored result");
            return null;
        }
    }

    synchronized private void runJobIfNecessaryAndPossible() {
        System.out.println(jobPool.toString() + workerPool.toString());
        final JobPool.JobEntry jobEntry = jobPool.firstJob();
        if (jobEntry == null) {
            log.debug("No Job in queue to run");
            return;
        }

        final WorkerPool.WorkerEntry workerEntry = workerPool.getBestApplicableWorker(
                jobEntry.jobMessage.getExecutorClassName());
        if (workerEntry == null) {
            log.debug("No available worker to run job");
            return;
        }
        log.debug("Fond worker to run job: "+ workerEntry);
        jobPool.jobTaken(jobEntry, workerEntry.serverLink);
        //notifyObservers();

        MessageSender.send(jobEntry.jobMessage, workerEntry.serverLink, new MessageSender.FailureHandler() {
            public void onFailure(ServerLink client) {
                log.debug("IOException while sending job to worker - removing worker");
                MasterServer.this.unregisterWorker(workerEntry.serverLink);
            }
        });
    }

}
