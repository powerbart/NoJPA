package dk.lessismore.nojpa.masterworker.client;

import dk.lessismore.nojpa.concurrency.WaitForValue;
import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.exceptions.MasterUnreachableException;
import dk.lessismore.nojpa.masterworker.master.MasterProperties;
import dk.lessismore.nojpa.masterworker.messages.JobMessage;
import dk.lessismore.nojpa.masterworker.messages.KillMessage;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteBeanMessage;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteResultMessage;
import dk.lessismore.nojpa.masterworker.messages.CancelJobMessage;
import dk.lessismore.nojpa.net.link.ClientLink;
import dk.lessismore.nojpa.properties.PropertiesProxy;
import dk.lessismore.nojpa.serialization.Serializer;
import dk.lessismore.nojpa.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class JobHandleToMasterProtocol<O> {

    private static final MasterProperties properties = PropertiesProxy.getInstance(MasterProperties.class);
    private static final Logger log = LoggerFactory.getLogger(JobHandleToMasterProtocol.class);

    private ClientLink clientLink = null;
    private Set<JobListener<O>> listeners = new CopyOnWriteArraySet<JobListener<O>>();
    public final Serializer serializer;
    private WaitForValue<Pair<Object, RuntimeException>> waitForValueOrNull = null;

    public JobHandleToMasterProtocol(Serializer serializer) {
        this.serializer = serializer;
        initConnection();
    }

    public ClientLink getClientLink() {
        return clientLink;
    }

    private void initConnection() {
        long start = System.currentTimeMillis();
        String host = properties.getHost();
        int port = properties.getClientPort();
        log.debug("1/2:Trying to establish connection to Master on "+host+":"+port);
        try {
            clientLink = new ClientLink(host, port);
        } catch (ConnectException e) {
            throw new MasterUnreachableException("Failed to connect to Master on "+host+":"+port+". "+e.getMessage());
        } catch (IOException e) {
            throw new MasterUnreachableException(e);
        }
        log.debug("2/2:Connection established.... Time("+ (System.currentTimeMillis() - start) +")");

        Thread callbackThread = new ClientCallbackThread<O>(this, clientLink, serializer);
        callbackThread.setDaemon(true);
        callbackThread.start();
    }

    public void sendRunJobRequest(String jobID, Class executorClass, Object jobData, JobListener<O> listener, long deadline) {
        removeAllJobListeners();
        String serializedJobDate = serializer.serialize(jobData);
        JobMessage jobMessage = new JobMessage(jobID, executorClass, serializedJobDate, deadline);
        try {
            clientLink.write(jobMessage);
            addJobListener(listener);
        } catch (IOException e) {
            throw new MasterUnreachableException(e);
        }
    }

    public void stopNicely(String jobID) {
        try {
            clientLink.write(new CancelJobMessage(jobID));
        } catch (IOException e) {
            throw new MasterUnreachableException(e);
        }
    }

    public void kill(String jobID) {
        try {
            clientLink.write(new KillMessage(jobID));
            close();
        } catch (IOException e) {
            throw new MasterUnreachableException(e);
        }
    }

    public void runMethodRemote(RunMethodRemoteBeanMessage runMethodRemoteBeanMessage, WaitForValue<Pair<Object, RuntimeException>> waitForValue) {
        try {
            waitForValueOrNull = waitForValue;
            clientLink.write(runMethodRemoteBeanMessage);
        } catch (IOException e) {
            throw new MasterUnreachableException(e);
        }
    }


    public void addJobListener(JobListener<O> listener) {
        listeners.add(listener);
    }


    public void removeJobListener(JobListener<O> listener) {
        listeners.remove(listener);
    }

    public void removeAllJobListeners() {
        listeners.clear();
    }

    public void setWaitForValueToNull() {
        this.waitForValueOrNull = null;
    }

    public void close() {
        if(waitForValueOrNull != null){
            waitForValueOrNull.setValue(new Pair<Object, RuntimeException>(null, new RuntimeException("ServerLink closed by Master/Worker")));
        }
        removeAllJobListeners();
        clientLink.close();
    }

    public void notifyException(RuntimeException exception)  {
        for (JobListener<O> listener: listeners) {
            listener.onException(exception);
        }
    }

    public void notifyResult(O result) {
        log.debug("notifyResult for listeners.size("+ listeners.size() +")");
        for (JobListener<O> listener: listeners) {
            listener.onResult(result);
        }
    }

    public void notifyRunMethodRemoteResult(RunMethodRemoteResultMessage runMethodRemoteResultMessage) {
        for (JobListener<O> listener: listeners) {
            listener.onRunMethodRemoteResult(runMethodRemoteResultMessage);
        }
    }

    public void notifyStatus(JobStatus status) {
        log.debug("notifyStatus for listeners.size("+ listeners.size() +")");
        if (status.equals(JobStatus.DONE)) notifyProgress(1.0);
        for (JobListener<O> listener: listeners) {
            listener.onStatus(status);
        }
    }

    public void notifyProgress(double progress) {
        for (JobListener<O> listener: listeners) {
            listener.onProgress(progress);
        }
    }

}
