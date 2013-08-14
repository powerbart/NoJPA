package dk.lessismore.reusable_v4.masterworker.client;

import dk.lessismore.reusable_v4.masterworker.master.MasterProperties;
import dk.lessismore.reusable_v4.masterworker.messages.*;
import dk.lessismore.reusable_v4.net.link.ClientLink;
import dk.lessismore.reusable_v4.masterworker.exceptions.MasterUnreachableException;
import dk.lessismore.reusable_v4.masterworker.executor.Executor;
import dk.lessismore.reusable_v4.masterworker.JobStatus;
import dk.lessismore.reusable_v4.serialization.Serializer;
import dk.lessismore.reusable_v4.properties.PropertiesProxy;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.net.ConnectException;

public class JobHandleToMasterProtocol<O> {

    private static final MasterProperties properties = PropertiesProxy.getInstance(MasterProperties.class);
    private static Logger log = Logger.getLogger(JobHandleToMasterProtocol.class);

    private ClientLink clientLink = null;
    private Set<JobListener<O>> listeners = new CopyOnWriteArraySet<JobListener<O>>();
    public final Serializer serializer;

    public JobHandleToMasterProtocol(Serializer serializer) {
        this.serializer = serializer;
        initConnection();
    }

    private void initConnection() {
        String host = properties.getHost();
        int port = properties.getClientPort();
        log.debug("Trying to establish connection to Master on "+host+":"+port);
        try {
            clientLink = new ClientLink(host, port);
        } catch (ConnectException e) {
            throw new MasterUnreachableException("Failed to connect to Master on "+host+":"+port+". "+e.getMessage());
        } catch (IOException e) {
            throw new MasterUnreachableException(e);
        }
        log.debug("Connection established");

        Thread callbackThread = new ClientCallbackThread<O>(this, clientLink, serializer);
        callbackThread.setDaemon(true);
        callbackThread.start();
    }

    public void sendRunJobRequest(String objectID, Class<? extends Executor> executorClass, Object jobData) {
        String serializedJobDate = serializer.serialize(jobData);
        JobMessage jobMessage = new JobMessage(objectID, executorClass, serializedJobDate);
        try {
            clientLink.write(jobMessage);
        } catch (IOException e) {
            throw new MasterUnreachableException(e);
        }
    }

    public void stopNicely() {
        try {
            clientLink.write(new StopMessage());
        } catch (IOException e) {
            throw new MasterUnreachableException(e);
        }
    }

    public void kill() {
        try {
            clientLink.write(new KillMessage());
        } catch (IOException e) {
            throw new MasterUnreachableException(e);
        }
    }

    public void runMethodRemote(RunMethodRemoteBeanMessage runMethodRemoteBeanMessage) {
        try {
            clientLink.write(runMethodRemoteBeanMessage);
        } catch (IOException e) {
            throw new MasterUnreachableException(e);
        }
    }



    public void addJobListener(JobListener<O> listener, String jobID) {
        if (listeners.isEmpty()) registerForCallbacks(jobID);
        listeners.add(listener);
    }

    private void registerForCallbacks(String jobID) {
        try {
            clientLink.write(new JobListenMessage(jobID));
        } catch (IOException e) {
            throw new MasterUnreachableException(e);
        }
    }

    public void removeJobListener(JobListener<O> listener) {
        listeners.remove(listener);
    }

    public void removeAllJobListeners() {
        listeners.clear();
    }

    public void close() {
        removeAllJobListeners();
        clientLink.close();
    }

    public void notifyException(RuntimeException exception)  {
        for (JobListener<O> listener: listeners) {
            listener.onException(exception);
        }
    }

    public void notifyResult(O result) {
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
