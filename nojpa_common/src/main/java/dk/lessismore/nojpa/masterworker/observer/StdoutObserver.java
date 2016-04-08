package dk.lessismore.nojpa.masterworker.observer;

import dk.lessismore.nojpa.masterworker.messages.observer.UpdateMessage;
import dk.lessismore.nojpa.masterworker.messages.observer.ObserverJobMessage;
import dk.lessismore.nojpa.masterworker.messages.observer.ObserverWorkerMessage;

import java.io.IOException;
import java.util.Collection;


public class StdoutObserver extends AbstractObserver {

    public static void main(String[] args) {
        new StdoutObserver();
    }

    @Override
    public void update(UpdateMessage updateMessage) {
        System.out.println("--------------update update update START -----------------------------------------------------------------------------");
        for (ObserverJobMessage job: updateMessage.getObserverJobMessages()) {
            System.out.format("[JOB] JobID(%s) Status(%s) ClassName(%s) Progress(%s) Date(%s) SequenceNumber(%s) Status(%s) WorkerFailureCount(%s) Worker(%s)\n",
                    job.getJobID(),
                    job.getStatus(),
                    job.getExecutorClassName().substring(job.getExecutorClassName().lastIndexOf(".") + 1),
                    job.getProgress(),
                    job.getDate().getTime(),
                    job.getSequenceNumber(),
                    job.getStatus(),
                    job.getWorkerFailureCount(),
                    job.getWorker());
        }

        for (ObserverWorkerMessage worker: updateMessage.getObserverWorkerMessages()) {
            System.out.format("[WORKER] Address(%s) Idle(%s) SystemLoad(%s) VmMemoryUsage(%s) Classes(%s) Problem(%s)\n",
                    worker.getAddress(),
                    worker.getIdle(),
                    worker.getSystemLoad(),
                    worker.getVmMemoryUsage(),
                    formatClassesNames(worker.getKnownClasses()),
                    worker.getProblem());
        }
        System.out.println("--------------update update update END -----------------------------------------------------------------------------");
    }

    @Override
    protected void onConnectionError(IOException e) {
        System.out.println("CONNECTION ERROR: " + e.getMessage());
    }


    protected String formatClassesNames(Collection<String> classNames) {
        StringBuilder builder = new StringBuilder();
        for (String className: classNames) {
            String[] split = className.split("\\.");
            builder.append(split[split.length -1]);
            builder.append(", ");
        }
        int last = builder.length();
        if (last > 2) {
            builder.delete(last-2, last);
        }
        return builder.toString();
    }
}
