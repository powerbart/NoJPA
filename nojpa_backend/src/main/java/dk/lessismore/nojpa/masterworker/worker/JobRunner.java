package dk.lessismore.nojpa.masterworker.worker;

import java.util.concurrent.Executor;

public class JobRunner {
    private final Thread thread;

    public JobRunner(Executor e) {
        thread = new Thread(new Runnable() {
            public void run() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }
}
