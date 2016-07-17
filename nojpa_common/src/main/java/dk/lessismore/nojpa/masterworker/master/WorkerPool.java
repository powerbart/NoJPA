package dk.lessismore.nojpa.masterworker.master;

import org.apache.log4j.Logger;
import dk.lessismore.nojpa.net.link.ServerLink;
import dk.lessismore.nojpa.masterworker.messages.observer.ObserverWorkerMessage;
import dk.lessismore.nojpa.properties.PropertiesProxy;

import java.util.*;

public class WorkerPool {

    private static final MasterProperties properties = PropertiesProxy.getInstance(MasterProperties.class);

    private static org.apache.log4j.Logger log = Logger.getLogger(WorkerPool.class);
    Map<ServerLink, WorkerEntry> pool = new HashMap<ServerLink, WorkerEntry>();

    public void addWorker(String[] knownClasses, ServerLink serverLink) {
        WorkerEntry workerEntry = new WorkerEntry(knownClasses, serverLink);
        pool.put(workerEntry.serverLink, workerEntry);
    }

    public void removeWorker(WorkerEntry workerEntry) {
        pool.remove(workerEntry.serverLink);
    }

    public void removeWorker(ServerLink serverLink) {
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
                    log.debug("Worker("+ entry +") not applicable: " + inapplicableReason);
//                }
                continue;
            }
            if (entry.knownClasses.contains(executorClass)) {
                if (stepEntry == null || entry.healthierThan(stepEntry)) {
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


    class WorkerEntry  {

        private WorkerEntry(String[] knownClasses, ServerLink serverLink) {
            this.knownClasses = new HashSet<String>(Arrays.asList(knownClasses));
            this.serverLink = serverLink;
            this.idle = true;
        }

        public Set<String> knownClasses;
        public ServerLink serverLink;
        public double systemLoad = 0;
        public double vmMemoryUsage = 0;
        public Map<String, Double> diskUsages = new HashMap<String, Double>();
        public boolean idle;

        public double health() {
            return systemLoad;
        }

        @Override
        public String toString() {
            return String.format("{Worker: %s:%S idle=%s, load=%s, vmMemory=%s, health=%s}",
                    serverLink.getOtherHost(), serverLink.getOtherPort(),
                    idle, systemLoad, vmMemoryUsage, health());
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
            for (String mountPoint: diskUsages.keySet()) {
                double usage = diskUsages.get(mountPoint);
                if (usage > properties.getWorkerCriticalDiskUsage() && !mountPoint.startsWith("/Vol") && !mountPoint.startsWith("/mnt") && !mountPoint.equals("/home") && !mountPoint.equals("/net") && !mountPoint.equals("/dev") && !mountPoint.equals("0")) {
                    return "v2: Disk usage on disk mounted on : "+ mountPoint + " is to high: " + usage;
                }
            }
            return null; // worker is applicable
        }

    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("---------------------------------- WorkerPool ----------------------------------\n");
        for (WorkerEntry workerEntry: pool.values()) {
            builder.append("  ");
            builder.append(workerEntry.toString());
            builder.append("\n");
        }
        return builder.toString();
    }
}
