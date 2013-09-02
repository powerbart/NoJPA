package dk.lessismore.nojpa.masterworker.master;

import org.apache.log4j.Logger;

public class ObserverNotifierThread extends Thread {

    private static final Logger log = Logger.getLogger(ObserverNotifierThread.class);
    private final MasterServer masterServer;
    private static final long NOTIFY_INTERVAL = 1000;
    private boolean running = true;

    public ObserverNotifierThread(MasterServer masterServer) {
        this.masterServer = masterServer;
    }


    @Override
    public void run() {
        while(running) {
            try {
                Thread.sleep(NOTIFY_INTERVAL);
                masterServer.notifyObservers();
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
