package dk.lessismore.nojpa.masterworker.messages.observer;

import java.util.Set;
import java.util.Map;

public class ObserverWorkerMessage {

    private Set<String> knownClasses;
    private String address;
    private double systemLoad;
    private double vmMemoryUsage;
    private Map<String, Double> diskUsages;
    private boolean idle;
    private String problem;

    public Set<String> getKnownClasses() {
        return knownClasses;
    }

    public void setKnownClasses(Set<String> knownClasses) {
        this.knownClasses = knownClasses;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getSystemLoad() {
        return systemLoad;
    }

    public void setSystemLoad(double systemLoad) {
        this.systemLoad = systemLoad;
    }

    public double getVmMemoryUsage() {
        return vmMemoryUsage;
    }

    public void setVmMemoryUsage(double vmMemoryUsage) {
        this.vmMemoryUsage = vmMemoryUsage;
    }

    public Map<String, Double> getDiskUsages() {
        return diskUsages;
    }

    public void setDiskUsages(Map<String, Double> diskUsages) {
        this.diskUsages = diskUsages;
    }

    public boolean getIdle() {
        return idle;
    }

    public void setIdle(boolean idle) {
        this.idle = idle;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getProblem() {
        return problem;
    }
}