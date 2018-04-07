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

public class JobHandleToMasterProtocol<O> {

    private static final MasterProperties properties = PropertiesProxy.getInstance(MasterProperties.class);
    private static final Logger log = LoggerFactory.getLogger(JobHandleToMasterProtocol.class);

    private ClientLink clientLink = null;
    private JobListener<O> listener = null;
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
            setJobListener(listener);
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


    public void setJobListener(JobListener<O> listener) {
        log.debug("setJobListener("+ listener +")");
        this.listener = listener;
    }


    public void removeJobListener(JobListener<O> listener) {
        log.debug("removeJobListener("+ listener +")");
        this.listener = null;
    }

    public void removeAllJobListeners() {
        log.debug("removeAllJobListeners("+ listener +")");
        this.listener = null;
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
        log.debug("notifyException("+ listener +")");
        if(listener != null){
            listener.onException(exception);
        } else {
            log.error("We don't have a listener");
        }
    }

    public void notifyResult(O result) {
        log.debug("notifyException("+ listener +")");
        if(listener != null){
            listener.onResult(result);
        } else {
            log.error("We don't have a listener");
        }
    }

    public void notifyRunMethodRemoteResult(RunMethodRemoteResultMessage runMethodRemoteResultMessage) {
        log.debug("notifyRunMethodRemoteResult("+ listener +")");
        if(listener != null){
            listener.onRunMethodRemoteResult(runMethodRemoteResultMessage);
        } else {
            log.error("We don't have a listener");
        }
    }

    public void notifyStatus(JobStatus status) {
        log.debug("notifyStatus("+ listener +")");
        if(listener != null){
            listener.onStatus(status);
        } else {
            log.error("We don't have a listener");
        }
    }


    public void notifyProgress(double progress) {
        log.debug("notifyProgress("+ listener +")");
        if(listener != null){
            listener.onProgress(progress);
        } else {
            log.error("We don't have a listener");
        }
    }

}
