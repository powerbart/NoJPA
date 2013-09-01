package dk.lessismore.nojpa.masterworker.bean.client;

import dk.lessismore.nojpa.masterworker.exceptions.MasterWorkerException;
import dk.lessismore.nojpa.masterworker.JobStatus;

public class BatchJobHandle<O> {


    /* *
     * @return QUEUED if all jobs are queued, DONE if all jobs are done, and IN_PROGRESS otherwise.
     */
    public JobStatus getStatus() throws MasterWorkerException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @return The average of the progress of all jobs.
     */
    public float progress() throws MasterWorkerException {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all queued jobs and tries to make each job in progress stop itself.
     */
    public void stopNicely() throws MasterWorkerException {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all queued jobs and terminates each job in progress.
     */
    public void kill() throws MasterWorkerException {
        throw new UnsupportedOperationException();
    }

    /**
     * @param listener The listener to be notified each time something changes.
     */
    public void addJobProgressListener(JobListener listener) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return The handles for the jobs in this batch.
     * If the batch is set to stop on the first exception, it throws this exception if it occurs.
     * Other exceptions may come from network communication problems.
     */
    public JobHandle[] getJobHandles() throws MasterWorkerException {
        throw new UnsupportedOperationException();
    }
}
