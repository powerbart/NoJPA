package dk.lessismore.nojpa.masterworker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class SystemHealth {

    private static final Logger log = LoggerFactory.getLogger(SystemHealth.class);

    private static OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    private static MemoryMXBean heapBean = ManagementFactory.getMemoryMXBean();


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
        if (!new File("/bin/df").exists()) {
            return diskUsages;
        }

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
//                log.debug("1: " + t);
                if (t.indexOf("%") != -1 && !str.contains("Volumes")) {
                    String tStr = t.substring(0, t.indexOf("%"));
                    int q = 0;
                    try {
                        q = Integer.parseInt(tStr);
                    } catch (Exception e){
                        System.out.println("When parsing disk usage in SystemHelth: " + e);
                    }
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
