package dk.lessismore.nojpa.masterworker.master;

import dk.lessismore.nojpa.properties.Properties;
import dk.lessismore.nojpa.properties.Required;
import dk.lessismore.nojpa.properties.Default;

public interface MasterProperties extends Properties {
    @Required
    public String getHost();
    @Default("9001")
    public int getClientPort();
    @Default("9002")
    public int getWorkerPort();
    @Default("9003")
    public int getObserverPort();
    @Required
    public String getStoreResultDir();
    @Default("1")
    public int getRetriesOnWorkerFailure();
    @Default("0.99")
    public double getWorkerCriticalDiskUsage();
    @Default("0.99")
    public double getWorkerCriticalVmMemoryUsage();
}
