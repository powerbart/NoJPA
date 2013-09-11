package dk.lessismore.nojpa.webservicelog.impl;

import dk.lessismore.nojpa.file.Files;
import dk.lessismore.nojpa.guid.GuidFactory;
import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import dk.lessismore.nojpa.utils.SuperIO;
import dk.lessismore.nojpa.webservicelog.CallLog;
import dk.lessismore.nojpa.webservicelog.CallLogService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.*;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class CpuInterceptor implements MethodInterceptor {

    private static Logger log = Logger.getLogger(CpuInterceptor.class);


    public static HashMap<String, CpuAttribute> cpuUsageMap = new HashMap<String, CpuAttribute>();


    public Object invoke(MethodInvocation invocation) throws Throwable {

        String className = invocation.getThis().getClass().getCanonicalName();
        String methodName = invocation.getMethod().getName();
        String key = className + ":" + methodName;
        long start = System.nanoTime();
        Object proceed = invocation.proceed();
        long end = System.nanoTime();
        long total = end - start;
        CpuAttribute cpuUsage = cpuUsageMap.get(key);
        if(cpuUsage == null){
            cpuUsage = new CpuAttribute(key, total, 1);
        } else {
            cpuUsage = new CpuAttribute(key, cpuUsage.getTotalTime() + total, cpuUsage.getNumberOfTimes() + 1);
        }
//        log.debug("cpuUsageMap.put("+ key +", "+ total +")");
        cpuUsageMap.put(key, cpuUsage);
        return proceed;
    }


    public static class CpuAttribute {

        String name;
        long totalTime;
        long numberOfTimes;

        public CpuAttribute(String name, long totalTime, long numberOfTimes){
            this.name = name;
            this.totalTime = totalTime;
            this.numberOfTimes = numberOfTimes;
        }


        public long getTotalTime() {
            return totalTime;
        }

        public long getNumberOfTimes() {
            return numberOfTimes;
        }

        public String getName() {
            return name;
        }
    }



}
