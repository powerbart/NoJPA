package dk.lessismore.nojpa.webservicelog;

import dk.lessismore.nojpa.webservicelog.impl.CallLogServiceImpl;

import java.util.Date;
import java.util.Calendar;

public interface CallLogService {
    public void ignore(Runnable action);
    public boolean isIgnoringThread(Thread thread);

    public CallLog getCallLogByID(String id, CallerIdentifierLog callerIdentifierLog);

    public CallLog[] getCallLogsByClass(
            String name, String method, int offset, int limit, int minMaxLogLevel,
            Date fromDateTime, Date toDateTime, CallerIdentifierLog callerIdentifierLog);

    public CallLog[] getCallLogs(int offset, int limit, int minMaxLoLevel, Date fromDateTime, Date toDateTime, 
                                 CallerIdentifierLog callerIdentifierLog);

    public CallLogServiceImpl.CallLogMethod[] getCallLogMethods(
            int offset, int limit, int minMaxLogLevel, Date fromDateTime, Date toDateTime,
            CallerIdentifierLog callerIdentifierLog);

    public CallLogServiceImpl.CallLogMethod getCallLogMethodLastCalls(
            String className, String methodName, int callCount, Date fromDateTime, Date toDateTime, int minMaxLogLevel,
            CallerIdentifierLog callerIdentifierLog);

    /*
    public CallLogServiceImpl.CallLogMethod[] getCallLogMethodSinceFlushWithProblems(
            double failureRateThreshold, int minimumCalls, int minMaxLogLevel,
            CallerIdentifierLog callerIdentifierLog);
      */
    
    public boolean getInterceptionEnabled();

    public void setInterceptionEnabled(boolean enabled);

    public CallLogServiceImpl.CallLogMethod[] getPerformance(int offset, int limit,
                                                             CallerIdentifierLog callerIdentifierLog);

    public CallLog[] getCallLogByParent(
            CallLog parent, int limitStart, int limitEnd, int minMaxLogLevel,
            Date fromDateTime, Date toDateTime, CallerIdentifierLog callerIdentifierLog);

    public int removeCallLogsFromBefore(Calendar calendar, CallerIdentifierLog callerIdentifierLog);

    public String fetchArguments(CallLog callLog);

   public String fetchResult(CallLog callLog);

   public String fetchLog(CallLog callLog);

   public String fetchException(CallLog callLog);
}
