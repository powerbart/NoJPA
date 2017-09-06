package dk.lessismore.nojpa.masterworker.master;

import dk.lessismore.nojpa.masterworker.messages.RegistrationMessage;
import dk.lessismore.nojpa.masterworker.messages.observer.ObserverWorkerMessage;
import dk.lessismore.nojpa.net.link.ServerLink;
import dk.lessismore.nojpa.properties.PropertiesProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkerPool {

    private static final MasterProperties properties = PropertiesProxy.getInstance(MasterProperties.class);

    private static final Logger log = LoggerFactory.getLogger(WorkerPool.class);
    Map<ServerLink, WorkerEntry> pool = new HashMap<ServerLink, WorkerEntry>();

    public synchronized void addWorker(RegistrationMessage registrationMessage, ServerLink serverLink) {
        WorkerEntry workerEntry = new WorkerEntry(registrationMessage.getHostname()+ ":" + registrationMessage.getFolder(), registrationMessage.getKnownClasses(), serverLink);
        pool.put(workerEntry.serverLink, workerEntry);
    }

//    public synchronized void removeWorker(WorkerEntry workerEntry) {
//        pool.remove(workerEntry.serverLink);
//    }

    public synchronized void removeWorker(ServerLink serverLink) {
        WorkerEntry entry = pool.get(serverLink); 
        if (entry != null) {
            pool.remove(serverLink);
        }
    }

    public WorkerEntry getBestApplicableWorker(String executorClass) {
        if (pool.isEmpty()) {
            log.info("No workers in pool");
            return null;
        }
        WorkerEntry stepEntry = null;
        for (WorkerEntry entry: pool.values()) {
            String inapplicableReason = entry.notApplicableReason();
            if (inapplicableReason != null) {
//                if(!inapplicableReason.equals("Worker is busy")){
//                    log.debug("Worker("+ entry +") not applicable: " + inapplicableReason);
//                }
                continue;
            }
            if (entry.knownClasses.contains(executorClass)) {
                if (stepEntry == null || entry.lastIdleStart < stepEntry.lastIdleStart) {
                    if(stepEntry != null){
                        log.debug("PreSelectedWorker("+ stepEntry +") losses to NewSelectedWorker("+ entry +")");
                    }
                    stepEntry = entry;
                }
            }

        }

        if (stepEntry == null) {
            log.warn("No applicable or compatible worker in pool(size:"+ pool.size() +") for executor: "+executorClass);
        } else {
            stepEntry.idle = false;
        }
        return stepEntry;
    }

    public WorkerEntry getWorkerEntry(ServerLink worker) {
        return pool.get(worker);
    }

    public boolean isIdle(ServerLink worker) {
        WorkerEntry entry = pool.get(worker);
        if (entry == null) {
            log.error("Worker does not exists");
            return false;
        }
        return entry.idle;
    }

    public void setIdle(boolean idle, ServerLink worker) {
        WorkerEntry entry = pool.get(worker);
        if (entry == null) {
            log.error("Worker does not exists - setIdle");
            return;
        }
        entry.idle = idle;
    }

    public void updateWorkerHealth(double systemLoad, double vmMemoryUsage, Map<String, Double> diskUsages, ServerLink serverLink) {
        WorkerEntry workerEntry = pool.get(serverLink);
        workerEntry.systemLoad = systemLoad;
        workerEntry.vmMemoryUsage = vmMemoryUsage;
        workerEntry.diskUsages = diskUsages;
    }

    public boolean applicable(ServerLink worker) {
        WorkerEntry entry = pool.get(worker);
        if (entry == null) {
            log.error("Worker does not exists");
            return false;
        }
        return entry.applicable();
    }

    public List<ObserverWorkerMessage> getObserverWorkerMessageList() {
        ArrayList<ObserverWorkerMessage> list = new ArrayList<ObserverWorkerMessage>();
        for (WorkerEntry workerEntry: pool.values()) {
            list.add(createObserverWorkerMessage(workerEntry));
        }
        return list;
    }

    private ObserverWorkerMessage createObserverWorkerMessage(WorkerEntry workerEntry) {
        ObserverWorkerMessage worker = new ObserverWorkerMessage();
        worker.setDiskUsages(workerEntry.diskUsages);
        worker.setAddress(workerEntry.serverLink.getOtherHostPort());
        worker.setIdle(workerEntry.idle);
        worker.setKnownClasses(workerEntry.knownClasses);
        worker.setSystemLoad(workerEntry.systemLoad);
        worker.setVmMemoryUsage(workerEntry.vmMemoryUsage);
        worker.setProblem(workerEntry.notApplicableReason());
        return worker;
    }


    static int TOTAL_COUNT_OF_INSTANCE = 1;

    public int getSize() {
        return pool.size();
    }

    class WorkerEntry  {

        private final String debugNameOfWorker;

        private WorkerEntry(String debugNameOfWorker, String[] knownClasses, ServerLink serverLink) {
            this.knownClasses = new HashSet<String>(Arrays.asList(knownClasses));
            this.debugNameOfWorker = debugNameOfWorker;
            this.serverLink = serverLink;
            this.idle = true;
            this.myID = TOTAL_COUNT_OF_INSTANCE++;
            StringBuilder b = new StringBuilder();
            for(int i = 0; i < knownClasses.length; i++){
                if(i > 0) b.append(", ");
                b.append(knownClasses[i].indexOf(".") != -1 ? knownClasses[i].substring(knownClasses[i].indexOf(".") + 1) : knownClasses[i]);
            }
            this.debugNameOfWorkerClasses = b.toString();
        }

        public Set<String> knownClasses;
        public ServerLink serverLink;
        public double systemLoad = 0;
        public double vmMemoryUsage = 0;
        public Map<String, Double> diskUsages = new HashMap<String, Double>();
        public boolean idle;
        public String debugNameOfWorkerClasses;
        private final int myID;

        public long rebootTime = System.currentTimeMillis();
        public long lastJobStart = -1;
        public long lastIdleStart = System.currentTimeMillis();
        public long totalIdleTime = 0;
        public long totalJobTime = 0;
        public long totalCountOfJobs = 1; //Should always be 1 (divide by zero)
        public long totalCountOfSuccesJobs = 0;

        public void jobReturnedStats() {
            lastIdleStart = System.currentTimeMillis();
            totalCountOfSuccesJobs++;
            totalJobTime = totalJobTime + (System.currentTimeMillis() - lastJobStart);
        }

        public void jobTakenStats() {
            lastJobStart = System.currentTimeMillis();
            totalCountOfJobs++;
            totalIdleTime = totalIdleTime + (System.currentTimeMillis() - lastIdleStart);
        }

        public String getIdleTime(){
//            return timeToString(idle && totalCountOfJobs > 1 ? (totalIdleTime + (System.currentTimeMillis() - lastIdleStart)) : totalIdleTime);
            return timeToString(totalIdleTime + (System.currentTimeMillis() - lastIdleStart));
        }

        public String getJobTime(){
            return timeToString(idle ? totalJobTime : (totalJobTime + (System.currentTimeMillis() - lastJobStart)));
        }

        public float getWorkLoad(){
            return (((float)totalJobTime) / ((float)(System.currentTimeMillis() - rebootTime)));
        }

        public String getTimeSinceLastJob(){
            return timeToString(idle && totalCountOfJobs > 1 ?  (System.currentTimeMillis() - lastJobStart) : 0);
        }



        private String timeToString(long l){
            long SEC = 1000;
            long MIN = 60 * SEC;
            long HOUR = 60 * MIN;
            long DAY = 24 * HOUR;

            if(l / DAY > 1){
                return printNice((((double)l) / ((double) DAY))) + "d";
            }
            if(l / HOUR > 1){
                return printNice((((double)l) / ((double) HOUR))) + "h";
            }
            if(l / MIN > 1){
                return printNice((((double)l) / ((double) MIN))) + "m";
            }
            return printNice((((double)l) / ((double) SEC))) + "s";
        }

        public int getMyID() {
            return myID;
        }

        public double health() {
            return systemLoad;
        }



        @Override
        public String toString() {
            return
                    "{" + rightAlign("Worker["+ myID +"]:", 12) + rightAlign(" idle="+ idle, 11) + rightAlign("load="+ printNice("" + systemLoad), 10) +" "+
                            (debugNameOfWorker != null && debugNameOfWorker.length() > 7 ? debugNameOfWorker : serverLink.getOtherHost())
                            +" JobTime:"+ getJobTime() +" IdleTime:"+ getIdleTime() +" jobs("+ totalCountOfSuccesJobs
                            +") succes("+ printNice("" + (((double) totalCountOfSuccesJobs)/ ((double) totalCountOfJobs)))
                            +") job/sec("+ printNice("" + (((double)(System.currentTimeMillis() - rebootTime))/ ((double) totalCountOfJobs))) +") lastIdleStart("+ (System.currentTimeMillis() - lastIdleStart) +") LastJob("+  getTimeSinceLastJob() +")}"

            ;
        }


        public String listRow() {
            return
                    "{" + rightAlign(""+ myID +"", 10)
                            + rightAlign(""+ idle, 8)
                            + rightAlign(""+ printNice(systemLoad), 8)
                            + rightAlign(""+ ("" + (debugNameOfWorker != null && debugNameOfWorker.length() > 7 ? debugNameOfWorker : serverLink.getOtherHost())), 42)
                            + rightAlign(""+ printNice(getWorkLoad()), 8)
                            + rightAlign(""+ (getJobTime()), 8)
                            + rightAlign(""+ (getIdleTime()), 8)
                            + rightAlign(""+ timeToString(System.currentTimeMillis() - rebootTime), 8)
                            + rightAlign(""+ timeToString(System.currentTimeMillis() - lastIdleStart), 13)
                            + rightAlign(""+ (idle ? "0" : timeToString(System.currentTimeMillis() - lastJobStart)), 8)
                            + rightAlign(""+ printNice((((double) totalCountOfSuccesJobs)/ ((double) totalCountOfJobs))), 8)
                            + rightAlign(""+ printNice(totalCountOfSuccesJobs), 13)
                            + rightAlign(""+ printNice((((double)(System.currentTimeMillis() - rebootTime))/ (((double) totalCountOfJobs) * 1000))), 17)
                            + ":::" + debugNameOfWorkerClasses
                            + "}"
                    ;
        }

        public String listTitle() {
            return
                    "{" + rightAlign("Worker No", 10)
                            + rightAlign("idle", 8)
                            + rightAlign("Load", 8)
                            + rightAlign("Name", 42)
                            + rightAlign("Load%", 8)
                            + rightAlign("JobT", 8)
                            + rightAlign("IdleT", 8)
                            + rightAlign("Reboot", 8)
                            + rightAlign("LIdle", 13)
                            + rightAlign("LastJ", 8)
                            + rightAlign("Succes", 8)
                            + rightAlign("Count", 13)
                            + rightAlign("J/sec", 17)
                            + ":::" + "Classes:"
                            + "}"
                    ;
        }

        private String rightAlign(String s, int totalSpace){
            int length = s.length();
            if(length > totalSpace){
                return s;
            } else {
                totalSpace = totalSpace - length;
                for(int i = 0; i < totalSpace; i++){
                    s = " " + s;
                }
                return s;
            }
        }

        private String printNice(long s){
            return String.format("%,d", s);
        }

        private String printNice(Number s){
            return String.format("%.02f", s.floatValue());
        }

        private String printNice(String s){
            return s != null && s.length() > 4 ? s.substring(0, 4) : s;
        }


        public boolean healthierThan(WorkerEntry entry) {
            return this.health() > entry.health();
        }

        public boolean applicable() {
            return notApplicableReason() == null;
        }

        /**
         * @return null if worker is healthy enough to be usable (applicable) or a String stating the problem.
         */
        public String notApplicableReason() {
            if (! idle) {
                return "Worker is busy";
            }
            if (vmMemoryUsage > properties.getWorkerCriticalVmMemoryUsage()) {
                return "Memory usage in VM too high: "+ vmMemoryUsage;
            }
//            for (String mountPoint: diskUsages.keySet()) {
//                double usage = diskUsages.get(mountPoint);
//                if (usage > properties.getWorkerCriticalDiskUsage() && !mountPoint.startsWith("/Vol") && !mountPoint.startsWith("/mnt") && !mountPoint.equals("/home") && !mountPoint.equals("/net") && !mountPoint.equals("/dev") && !mountPoint.equals("0")) {
//                    return "v2: Disk usage on disk mounted on : "+ mountPoint + " is to high: " + usage;
//                }
//            }
            return null; // worker is applicable
        }

    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("---------------------------------- WorkerPool ----------------------------------\n");
        boolean first = true;
        for (WorkerEntry workerEntry: pool.values()) {
            if(first){
                first = false;
                builder.append(workerEntry.listTitle());
                builder.append("\n");
            }
            builder.append(workerEntry.listRow());
            builder.append("\n");
        }
        return builder.toString();
    }
}
