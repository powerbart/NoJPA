package dk.lessismore.nojpa.masterworker.messages;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class HealthMessage {

    private double systemLoad = 0;
    private double vmMemoryUsage = 0;
    private Map<String, Double> diskUsages = new HashMap<String, Double>();

    public HealthMessage() {
    }

    public HealthMessage(double systemLoad, double vmMemoryUsage, Map<String, Double> diskUsages) {
        this.systemLoad = systemLoad;
        this.vmMemoryUsage = vmMemoryUsage;
        this.diskUsages = diskUsages;
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


}