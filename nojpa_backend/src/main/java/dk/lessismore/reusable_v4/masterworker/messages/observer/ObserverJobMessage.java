package dk.lessismore.reusable_v4.masterworker.messages.observer;

import dk.lessismore.reusable_v4.masterworker.JobStatus;

import java.util.HashSet;
import java.util.Calendar;
import java.util.List;


public class ObserverJobMessage {
    private String jobID;
    private int sequenceNumber;
    private String executorClassName;
    private String serializedJobData;
    private List<String> listeningClients;
    private double progress;
    private double priority;
    private JobStatus status;
    private Calendar date;
    private String worker;
    private int workerFailureCount;

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getExecutorClassName() {
        return executorClassName;
    }

    public void setExecutorClassName(String executorClassName) {
        this.executorClassName = executorClassName;
    }

    public String getSerializedJobData() {
        return serializedJobData;
    }

    public void setSerializedJobData(String serializedJobData) {
        this.serializedJobData = serializedJobData;
    }

    public List<String> getListeningClients() {
        return listeningClients;
    }

    public void setListeningClients(List<String> listeningClients) {
        this.listeningClients = listeningClients;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public int getWorkerFailureCount() {
        return workerFailureCount;
    }

    public void setWorkerFailureCount(int workerFailureCount) {
        this.workerFailureCount = workerFailureCount;
    }
}
