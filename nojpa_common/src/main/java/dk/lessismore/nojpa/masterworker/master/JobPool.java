package dk.lessismore.nojpa.masterworker.master;

import dk.lessismore.nojpa.net.link.ServerLink;
import dk.lessismore.nojpa.masterworker.messages.JobResultMessage;
import dk.lessismore.nojpa.masterworker.messages.JobMessage;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteBeanMessage;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteResultMessage;
import dk.lessismore.nojpa.masterworker.messages.observer.ObserverJobMessage;
import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.exceptions.WorkerExecutionException;
import dk.lessismore.nojpa.properties.PropertiesProxy;
import dk.lessismore.nojpa.utils.MaxSizeArray;

import java.util.*;
import java.text.SimpleDateFormat;
import java.io.IOException;

public class JobPool {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JobPool.class);
    private static final MasterProperties properties = PropertiesProxy.getInstance(MasterProperties.class);

    static private int jobEntrySequenceNumberCounter = 0;
    private final Map<ServerLink, String> clientMap = new HashMap<ServerLink, String>();
    private final Map<ServerLink, String> workerMap = new HashMap<ServerLink, String>();
    private final Map<String, JobEntry> pool = new HashMap<String, JobEntry>();
    private final SortedSet<JobEntry> queue = new TreeSet<JobEntry>(new PriorityComparator());
    private final MaxSizeArray<JobEntry> last5SuccesJobs = new MaxSizeArray<JobEntry>(5);
    private final MessageSender.FailureHandler failureHandler = new MessageSender.FailureHandler() {
        public void onFailure(ServerLink client) {
            removeListener(client);
        }
    };


    public void runMethodRemote(RunMethodRemoteBeanMessage runMethodRemoteBeanMessage, ServerLink serverLink) throws IOException {
        JobPool.JobEntry jobEntry = pool.get(runMethodRemoteBeanMessage.getJobID());
        jobEntry.runMethodRemote(runMethodRemoteBeanMessage, serverLink);
    }




    public void addJob(JobMessage job) {
        if(pool.get(job.getJobID()) == null) {
            JobEntry jobEntry = new JobEntry(job);
            pool.put(job.getJobID(), jobEntry);
            queue.add(jobEntry);
            log.debug("Added job: " + jobEntry);
        }
    }


    /**
     * Add listener for jobID if the corresponding job entry exists in pool
     * @param jobID ID for the job to listen to.
     * @param client the link to the client
     * @return True if the listener could be added False otherwise.
     */
    public boolean addListener(String jobID, ServerLink client) {
        clientMap.put(client, jobID);
        JobEntry jobEntry = pool.get(jobID);
        if (jobEntry != null) {
            if (jobEntry.clients == null) jobEntry.clients = new HashSet<ServerLink>();
            jobEntry.clients.add(client);
            if (jobEntry.result == null) {
                MessageSender.sendStatusToClient(jobID, jobEntry.getStatus(), client, failureHandler);
                MessageSender.sendProgressToClient(jobID, jobEntry.progress, client, failureHandler);
            } else {
                MessageSender.sendResultToClient(jobEntry.result, client, failureHandler);
            }
            return true;
        } else {
            log.debug("No job entry in job pool - can't add listener");
            return false;
        }
    }

    public void removeListener(ServerLink client) {
        String jobID = clientMap.get(client);
        if (jobID == null) {
            log.info("No jobID found for client - cant remove listener");
            return;
        }
        clientMap.remove(client);
        JobEntry jobEntry = pool.get(jobID);
        if (jobEntry == null) {
            log.debug("No jobEntry exist - cant remove listener");
            return;
        }
        if (jobEntry.clients == null) {
            log.debug("No listening clients exist - cant remove listener");
            return;
        }
        jobEntry.clients.remove(client);
    }

    public void updateJobProgress(String jobID, double progress) {
        JobEntry jobEntry = pool.get(jobID);
        if (jobEntry != null) {
            jobEntry.progress = progress;
            fireOnProgress(jobEntry);
        } else {
            log.error("Trying to set progress for job not in pool");
        }
    }

    public void setRunMethodRemoteResultMessage(RunMethodRemoteResultMessage runMethodRemoteResultMessage){
        if (runMethodRemoteResultMessage == null) {
            log.error("runMethodRemoteResultMessage must be a non-null value");
            return;
        }
        String jobID = runMethodRemoteResultMessage.getJobID();
        JobEntry jobEntry = pool.get(jobID);
        if (jobEntry != null) {
            fireOnRunMethodRemoteResult(jobEntry, runMethodRemoteResultMessage);
        } else {
            log.error("Trying to set result for job not in pool");
        }
    }


    Calendar lastResult;
    long resultTotalCounter = 0;
    long resultLast100Time = 0;
    long resultStartTime = System.currentTimeMillis();
    public void setResult(JobResultMessage result) {
        if (result == null) {
            log.error("Result must be a non-null value");
            return;
        }
        lastResult = Calendar.getInstance();
        String jobID = result.getJobID();
        JobEntry jobEntry = pool.get(jobID);
        if (jobEntry != null) {
            jobEntry.result = result;
            last5SuccesJobs.add(jobEntry);
            if(resultTotalCounter++ % 100 == 0){
                resultLast100Time = System.currentTimeMillis();
            }
//            removeWorker(jobEntry);
            fireOnResult(jobEntry, result);
        } else {
            log.error("Trying to set result for job not in pool");
        }
    }

    public JobEntry firstJob() {
        try {
            return queue.first();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void jobTaken(JobEntry jobEntry, ServerLink worker) {
        log.debug(" * JOB TAKEN: "+jobEntry);
        jobEntry.jobTakenDate = Calendar.getInstance();
        setWorker(jobEntry, worker);
        queue.remove(jobEntry);
        fireOnStatus(jobEntry);
    }

    public void requeueJobIfRunning(ServerLink worker) {
        String jobID = workerMap.get(worker);
        if (jobID == null) return;
        JobEntry entry = pool.get(jobID);
        if (entry == null) return;

        if (! entry.worker.equals(worker)) { //TODO remove test later
            log.error("workerMap and job entry is inconsistent");
            return;
        }
        if (entry.result != null) { //TODO remove test later
            log.error("workerMap and job entry is inconsistent - entry has result");
            return;
        }

        removeWorker(entry);
        entry.progress = 0;
        entry.workerFailureCount++;
        if (entry.workerFailureCount <= properties.getRetriesOnWorkerFailure()) {
            queue.add(entry);
            fireOnProgress(entry);
            fireOnStatus(entry);
            log.warn("Re added job to queue "+entry);
        } else {
            log.error("Worker failure count exceeded limit ("+properties.getRetriesOnWorkerFailure()+") for entry: "+entry);
            log.error("Sending back exception");
            JobResultMessage jobResultMessage = new JobResultMessage(entry.jobMessage.getJobID());
            jobResultMessage.setMasterException(new WorkerExecutionException(""+entry.workerFailureCount+" workers dies while execution this job"));
            entry.result = jobResultMessage;
            fireOnResult(entry, jobResultMessage);
        }
    }


    // Private stuff

    private void fireOnStatus(JobEntry jobEntry) {
        String jobID = jobEntry.jobMessage.getJobID();
        JobStatus jobStatus = jobEntry.getStatus();
        if (jobEntry.clients == null || jobEntry.clients.isEmpty()) return;
        for (ServerLink client: getListeningClientsCloned(jobEntry)) {
            MessageSender.sendStatusToClient(jobID, jobStatus, client, failureHandler);
        }
    }

    private void fireOnProgress(JobEntry jobEntry) {
        String jobID = jobEntry.jobMessage.getJobID();
        if (jobEntry.clients == null || jobEntry.clients.isEmpty()) return;
        for (ServerLink client: getListeningClientsCloned(jobEntry)) {
            MessageSender.sendProgressToClient(jobID, jobEntry.progress, client, failureHandler);
        }
    }

    private void fireOnRunMethodRemoteResult(JobEntry jobEntry, RunMethodRemoteResultMessage runMethodRemoteResultMessage) {
        log.debug("fireOnRunMethodRemoteResult sending : " + runMethodRemoteResultMessage);
        if (jobEntry.clients == null || jobEntry.clients.isEmpty()){
            log.warn("No clients to send the result to .... " + runMethodRemoteResultMessage);
            return;
        }
        for (ServerLink client: getListeningClientsCloned(jobEntry)) {
            log.debug("fireOnRunMethodRemoteResult sending to ("+ client +") -> " + runMethodRemoteResultMessage);
            MessageSender.sendRunMethodRemoteResultOfToClient(runMethodRemoteResultMessage, client, failureHandler);
        }
    }

    private void fireOnResult(JobEntry jobEntry, JobResultMessage result) {
        jobEntry.jobDoneDate = Calendar.getInstance();
        if (jobEntry.clients == null || jobEntry.clients.isEmpty()) return;
        for (ServerLink client: getListeningClientsCloned(jobEntry)) {
            MessageSender.sendResultToClient(result, client, failureHandler);
            client.close();
        }
        removeJob(result.getJobID());
    }

    private void removeJob(String jobID) {
        JobEntry jobEntry = pool.get(jobID);
        if (jobEntry != null) {
            pool.remove(jobID);
            queue.remove(jobEntry);
            log.debug("Removed job: "+ jobEntry);
        }
    }

    @SuppressWarnings("unchecked cast")
    private HashSet<ServerLink> getListeningClientsCloned(JobEntry jobEntry) {
        return (HashSet<ServerLink>) jobEntry.clients.clone();
    }

    private void setWorker(JobEntry jobEntry, ServerLink worker) {
        workerMap.put(worker, jobEntry.jobMessage.getJobID());
        jobEntry.worker = worker;
    }

    private void removeWorker(JobEntry jobEntry) {
        workerMap.remove(jobEntry.worker);
        jobEntry.worker = null;
    }


    class JobEntry {

        public final int sequenceNumber;
        public JobMessage jobMessage;
        public HashSet<ServerLink> clients;
        public JobResultMessage result;
        public double progress = 0;
        public double priority = 1;
        public Calendar date = Calendar.getInstance();
        public Calendar jobTakenDate = null;
        public Calendar jobDoneDate = null;
        public ServerLink worker;
        public int workerFailureCount = 0;

        private JobEntry(JobMessage jobMessage) {
            this.sequenceNumber = jobEntrySequenceNumberCounter;
            jobEntrySequenceNumberCounter++;
            this.jobMessage = jobMessage;
        }


        public JobStatus getStatus() {
            if (result != null) return JobStatus.DONE;
            else if (worker != null) return JobStatus.IN_PROGRESS;
            else return JobStatus.QUEUED;
        }

        public void runMethodRemote(RunMethodRemoteBeanMessage runMethodRemoteBeanMessage, ServerLink serverLink) throws IOException {
            while (getStatus().equals(JobStatus.QUEUED)) {
                try {
                    log.error("JobEntry thread has no assigned worker. The thread with sleep for 150ms and recheck");
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    log.error("JobEntry thread was interrupted " + e.getMessage());
                }
            }
            worker.write(runMethodRemoteBeanMessage);
        }


        @Override
        public String toString() {
            return String.format("{Job-%s:%s status=%s, result=%s progress=%s, priority=%s, rerun=%s,  w=%s, created=%s, taken=%s, result=%s}",
                    getSimpleName(jobMessage.getExecutorClassName()),
                    jobMessage.getJobID(),
                    getStatus(),
                    getResultType(result),
                    progress,
                    priority,
                    workerFailureCount,
                    (worker != null ? "" + worker.getOtherHost() +":"+ worker.getOtherPort() : "null"),
                    (new SimpleDateFormat("yyyyMMMdd HH:mm:ss")).format(date.getTime()),
                    (jobTakenDate != null ? (new SimpleDateFormat("MMMdd HH:mm:ss")).format(jobTakenDate.getTime()) : "-"),
                    (jobDoneDate != null ? (new SimpleDateFormat("MMMdd HH:mm:ss")).format(jobDoneDate.getTime()) : "-")
                    );
        }


        private String getResultType(JobResultMessage result) {
            if (result == null) return "None";
            else if (result.hasException()) return "Exception";
            else return "Value";
        }

        private String getSimpleName(String name) {
            if(name == null || name.indexOf(".") == -1) return "" + name;
            return name.substring(name.lastIndexOf('.')+1);
        }

    }


    private class PriorityComparator implements Comparator<JobEntry> {
        public int compare(JobEntry entry1, JobEntry entry2) {
            if (entry1.equals(entry2)) return 0;
            int c1 = Double.compare(entry1.priority, entry2.priority);
            if (c1 != 0) return c1;
            else return entry1.sequenceNumber - entry2.sequenceNumber;
        }
    }

    public List<ObserverJobMessage> getObserverJobMessageList() {
        ArrayList<ObserverJobMessage> list = new ArrayList<ObserverJobMessage>();
        for (JobEntry jobEntry: pool.values()) {
            list.add(createObserverJobMessage(jobEntry));
        }
        return list;
    }

    private ObserverJobMessage createObserverJobMessage(JobEntry jobEntry) {
        ObserverJobMessage job = new ObserverJobMessage();
        job.setJobID(jobEntry.jobMessage.getJobID());
        job.setSequenceNumber(jobEntry.sequenceNumber);
        job.setDate(jobEntry.date);
        job.setExecutorClassName(jobEntry.jobMessage.getExecutorClassName());
        if (jobEntry.clients != null) {
            ArrayList<String> clients = new ArrayList<String>(jobEntry.clients.size());
            for (ServerLink client: jobEntry.clients) {
                clients.add(client.getOtherHostPort());
            }
            job.setListeningClients(clients);
        }
        job.setPriority(jobEntry.priority);
        job.setProgress(jobEntry.progress);
        job.setSerializedJobData(jobEntry.jobMessage.getSerializedJobData());
        job.setStatus(jobEntry.getStatus());
        if (jobEntry.worker != null) {
            job.setWorker(jobEntry.worker.getOtherHostPort());
        }
        job.setWorkerFailureCount(jobEntry.workerFailureCount);
        return job;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("---------------------------------- JobPool ----------------------------------\n");
        builder.append("IN QUEUE:\n");
        for (JobEntry jobEntry: queue) {
            builder.append("  ");
            builder.append(jobEntry.toString());
            builder.append("\n");
        }
        builder.append("OTHERS:\n");
        for (JobEntry jobEntry: pool.values()) {
            if (queue.contains(jobEntry)) continue;
            builder.append("  ");
            builder.append(jobEntry.toString());
            builder.append("\n");
        }
        builder.append("LAST-5-RESULTS:\n");
        for (Iterator<JobEntry> iterator = last5SuccesJobs.iterator(); iterator.hasNext(); ) {
            JobEntry jobEntry = iterator.next();
            builder.append("  ");
            builder.append(jobEntry.toString());
            builder.append("\n");
        }
        builder.append("STATS: ");
        builder.append("Last-success(" +  (lastResult != null ? "" + lastResult.getTime() : " -NO RESULTS.... yet...! ") +") ");
        builder.append("queue.size("+ queue.size() +") ");
        builder.append("TOTAL("+ resultTotalCounter +") ");
        builder.append("LAST-"+ (resultTotalCounter % 100) +"-AVG("+ ((System.currentTimeMillis() - resultLast100Time)/(1+(resultTotalCounter % 100))) +") ");
        builder.append('\n');
        return builder.toString();
    }
}
