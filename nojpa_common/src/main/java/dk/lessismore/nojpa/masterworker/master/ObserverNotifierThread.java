package dk.lessismore.nojpa.masterworker.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObserverNotifierThread extends Thread {

    private static final Logger log = LoggerFactory.getLogger(ObserverNotifierThread.class);
    private final MasterServer masterServer;
    private static final long NOTIFY_INTERVAL = 10000;
    private boolean running = true;

    public ObserverNotifierThread(MasterServer masterServer) {
        this.masterServer = masterServer;
    }


    @Override
    public void run() {
        while(running) {
            try {
                masterServer.notifyObservers();
                Thread.sleep(NOTIFY_INTERVAL);
            } catch (InterruptedException e) {
                log.debug("Observer notifier interrupted");
            }
        }
        log.debug("ObserverNotifierThread stopped");
   }

    public void stopThread() {
        running = false;
        this.interrupt();
    }
}
