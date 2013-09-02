package dk.lessismore.nojpa.masterworker;

import org.apache.log4j.Logger;
import dk.lessismore.nojpa.masterworker.executor.SumExecutor;

import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.io.InputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;

public class SystemHealth {

    private static final Logger log = Logger.getLogger(SystemHealth.class);

    private static OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    private static MemoryMXBean heapBean = ManagementFactory.getMemoryMXBean();

    public static void main(String[] args) throws IOException {

        SumExecutor executor = new SumExecutor();
        executor.run(10000000l);
        
        // CPU
        System.out.println("getSystemLoadAverage() = " + getSystemLoadAverage());

        // MEM
        System.out.println("getVmMemoryUsage() = " + getVmMemoryUsage());

        // DISK
        Map<String, Double> usageMap = getDiskUsages();
        for (String mountPoint: usageMap.keySet()) {
            double usage = usageMap.get(mountPoint);
            System.out.println("Mount point: " + mountPoint + " "+usage);
        }
    }

    /**
     * @return the system load average; or a negative value if not available.
     */
    public static double getSystemLoadAverage() {
        return operatingSystemMXBean.getSystemLoadAverage();
    }

    /**
     * @return a number between 0 and 1 indicating the VM memoty usage.
     */
    public static double getVmMemoryUsage() {
        float usedMemory =  heapBean.getHeapMemoryUsage().getUsed();
        float totalMemory =  heapBean.getHeapMemoryUsage().getMax();
        return usedMemory/totalMemory;
        //int memUsage = (int) (100f * usedMemory / totalMemory);
        //Runtime runtimeEnv = Runtime.getRuntime();
        //return ((memUsage * ((((float) runtimeEnv.totalMemory() / 1000000f)) / ((float) runtimeEnv.maxMemory() / 1000000f))));
    }

    /**
     * @return mount point mapped to disk usage where usage > 50%.
     */
    public static Map<String, Double> getDiskUsages() {
        Map<String, Double> diskUsages = new HashMap<String, Double>();

        try {
            byte[] bytes = new byte[1024];
            Runtime man = Runtime.getRuntime();
            Process p;
            p = man.exec("/bin/df -h");
            InputStream in = p.getInputStream();
            int length = in.read(bytes);
            String str = (length != -1 ? (new String(bytes, 0, length)) : "");
            in.close();
            int maxQ = 0;
            StringTokenizer diskTok = new StringTokenizer(str.substring(str.indexOf("\n")), "\n\t ");
            while (diskTok.hasMoreTokens()) {
                String t = diskTok.nextToken();
                if (t.indexOf("%") != -1) {
                    String tStr = t.substring(0, t.indexOf("%"));
                    int q = Integer.parseInt(tStr);
                    maxQ = maxQ < q ? q : maxQ;
                    if (q > 50) {
                        String mountPoint = diskTok.nextToken();
                        diskUsages.put(mountPoint, (double)q/100.0);
                    }
                }
            }
            InputStream err = p.getErrorStream();
            //length = err.read(bytes);
            //if (length != -1) {
            //    log.warn(new String(bytes, 0, length));
            //}
            err.close();
            p.destroy();
        } catch (IOException e) {
            log.error("Failed to determine system disk diskUsages.", e);
        }
        return diskUsages;
    }
}
