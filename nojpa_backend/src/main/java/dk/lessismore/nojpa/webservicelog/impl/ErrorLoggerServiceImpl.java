package dk.lessismore.nojpa.webservicelog.impl;

import dk.lessismore.nojpa.file.Files;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.*;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import java.io.*;
import java.util.*;

import dk.lessismore.nojpa.webservicelog.CallLog;
import dk.lessismore.nojpa.webservicelog.CallLogService;
import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.utils.SuperIO;
import dk.lessismore.nojpa.guid.GuidFactory;

/**
 * Intercepts method calls to log any errors they might cause.
 * An error in this context is either an exception or a logging
 * event above or equal to the specified level
 * (Level.ALL_INT by default). If any of those occur,
 * everything that the method logged will be saved, along with
 * the exception it throwed, if any, and it's arguments, return
 * value and method name.
 */
public class ErrorLoggerServiceImpl implements MethodInterceptor {
    public final static int EXCEPTION_LEVEL = Level.OFF_INT;

    private static Logger log = Logger.getLogger(ErrorLoggerServiceImpl.class);

    private static final File directory = CallLogServiceImpl.getDirectory();

    private int level = Level.ALL_INT;
    private final CallLogService callLogService = CallLogServiceImpl.getInstance();
    private final Map<Thread, CallLog> callStack = new HashMap<Thread, CallLog>();
    private Map<String, Set<String>> debugClassMethods = new HashMap<String, Set<String>>();
    private Set<String> debugClasses = new HashSet<String>();
    private String mailToAddress = "seb@exiqon.com";

    public void setMailToAddress(String mailAddress) {
        this.mailToAddress = mailAddress;
    }

    /**
     * Enables or disables full call logging for a specific method, or if methodName is null,
     * a specific class (className must not be null).
     * It must be proxied by this object by some other means.
     */
    public void setDebug(String className, String methodName, boolean logEverything) {
        if(className == null) throw new NullPointerException("className must not be null");
        if(methodName == null) {
            if(logEverything) debugClasses.add(className);
            else debugClasses.remove(className);
        } else {
            if(!debugClassMethods.containsKey(className)) {
                debugClassMethods.put(className, new HashSet<String>());
            }
            Set<String> methods = debugClassMethods.get(className);
            if(logEverything) methods.add(methodName);
            else methods.remove(methodName);
        }
    }

    /**
     * Returns true if and only if full call logging is enabled for a specific method,
     * or if methodName is null, a specific class (className must not be null).
     */
    public boolean getDebug(String className, String methodName) {
        if(className == null) throw new NullPointerException("className must not be null");
        if(debugClasses.contains(className))
        if(methodName == null) return false;
        if(!debugClassMethods.containsKey(className)) return false;
        Set<String> methods = debugClassMethods.get(className);
        return methods.contains(methodName);
    }

    /**
     * Returns the minimum log level for which to log.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Set logging level - use Level.OFF_INT to turn off the
     * logging from this service completely, except for methods or classes
     * for which debugging has been enabled through setDebug.
     * Use Level.ALL_INT to log every single call.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        //dk.lessismore.reusable_v3.utils.Timer timer = new dk.lessismore.reusable_v3.utils.Timer("ErrorLoggerServiceImpl.invoke", "/tmp/delete.txt");
        //timer.tmpTime("1:");
        if(callLogService.isIgnoringThread(Thread.currentThread())) return invocation.proceed();
        String className = invocation.getThis().getClass().getCanonicalName();
        String methodName = invocation.getMethod().getName();
        boolean mustLog = getDebug(className, methodName);
        if(level == Level.OFF_INT && !mustLog) return invocation.proceed();
        File tmpLogFile = File.createTempFile("logFile." + className + "." + methodName, ".log");
        FileWriter stringWriter = new SameThreadStringWriter(tmpLogFile);
        InterestingFilter filter = new InterestingFilter();
        Appender appender = new WriterAppender(new PatternLayout("%d [%t] %-5p %c - %m%n"), stringWriter);
        appender.addFilter(filter);
        //timer.tmpTime("3:");
        Logger.getRootLogger().addAppender(appender);
        Object result = null;
        Throwable exception = null;
        //String logText;
        //timer.tmpTime("4:");
        long time = System.currentTimeMillis();
        CallLog callLog = ModelObjectService.create(CallLog.class);
        //timer.tmpTime("5:");
        callLog.setFailed(-1); callLog.setMaxLogLevel(-1); callLog.setMilliTime(-1);
        //timer.tmpTime("6:");
        CallLog parentCallLog = callStack.get(Thread.currentThread());

        callLog.setParent(parentCallLog);
        callLog.setClassName(className);
        callLog.setMethodName(methodName);

        // TODO: Why was this ever saved here? Does it have some side effects on the object that we need before the real save?
        //ModelObjectService.save(callLog);

        callStack.put(Thread.currentThread(), callLog);
        //timer.tmpTime("8:");
        try {
            //timer.tmpTime("8-1:");
//            log.debug(" --------------------------- "+ invocation.getThis().getClass().getSimpleName() + "." + invocation.getMethod().getName() +" --------------------------- ");
            result = invocation.proceed();
            //timer.tmpTime("8-2:");
            time = System.currentTimeMillis() - time;
        } catch (Throwable t) {
            time = System.currentTimeMillis() - time;
            exception = t;
        } finally {
            //timer.tmpTime("9:");
            Logger.getRootLogger().removeAppender(appender);
            appender.close();
            stringWriter.close();
            //timer.tmpTime("10:");
            callStack.put(Thread.currentThread(), parentCallLog);
            //timer.tmpTime("11:");
        }

        try {
            if(((exception != null || level >= Level.WARN_INT) || (callLogService.getInterceptionEnabled() && (mustLog || level == Level.ALL_INT || filter.maxLevel > level)))) {
                //timer.tmpTime("12:");
                int maxLogLevel = exception != null ? EXCEPTION_LEVEL : filter.maxLevel;
                callLog.setMaxLogLevel(maxLogLevel);
                callLog.setMilliTime(time);
                callLog.setFailed(callLog.getMaxLogLevel() >= Level.ERROR_INT ? 1.0 : 0.0);
                File path = new File(directory, (CallLogServiceImpl.getDateFormat()).format(new Date()));
                path.mkdir();
                Writer writer = null;
                try {
                    writer = new FileWriter(new File(path, callLog.getObjectID() + ".arguments"));
                    for(Object argument: invocation.getArguments()) {
                        if(argument != null) writer.write(argument.getClass().getSimpleName() + ":" );
                        writer.write(toReadableString(argument));
                        writer.write("\n\n");
                    }
                    writer.close();
                    writer = new FileWriter(new File(path, callLog.getObjectID() + ".result"));
                    writer.write(result != null ? result.getClass().getSimpleName() + " : " : "");
                    writer.write(toReadableString(result));
                    writer.close();
                    File fileLogName = new File(path, callLog.getObjectID() + ".log");
                    Files.copy(tmpLogFile, fileLogName);
                    writer = new FileWriter(new File(path, callLog.getObjectID() + ".exception"));
                    if(exception != null) {
                        PrintWriter exceptionPrinter = new PrintWriter(writer);
                        exception.printStackTrace(exceptionPrinter);
                        exceptionPrinter.close();
                    }
                    writer.close();
                } finally {
                    if(writer != null) writer.close();
                }
                ModelObjectService.save(callLog);
                if(level >= Level.ERROR_INT || exception != null) {
                    String mailTo = mailToAddress;
                    String mailFrom = "linuxsupport@exiqon.com";
                    String mailSmtpServer = "10.0.0.188";
                    String mailTitle = "(CallLog) " +
                            (maxLogLevel == Level.ERROR_INT ? "Error in " :
                            (maxLogLevel == Level.FATAL_INT) ? "Fatal error in " :
                            (maxLogLevel == EXCEPTION_LEVEL) ? "Exception in " : "Problem with ") +
                            className + "." + methodName;
                    StringBuilder builder = new StringBuilder();
                    builder.append("System.getenv(\"USER\") = " + System.getenv("USER"));
                    builder.append("System.getenv(\"HOST\") = " + System.getenv("HOST"));
                    builder.append("System.getenv(\"HOSTNAME\") = " + System.getenv("HOSTNAME"));
                    builder.append("System.getenv(\"LOGNAME\") = " + System.getenv("LOGNAME"));
                    builder.append("System.getenv(\"PWD\") = " + System.getenv("PWD"));

                    builder.append("Arguments: \r\n");
                    for(Object argument: invocation.getArguments()) {
                        if(argument != null) builder.append(argument.getClass().getSimpleName() + ":" );
                        builder.append(toReadableString(argument));
                        builder.append("\r\n");
                    }
                    builder.append("\r\n");
                    builder.append("Result: \r\n");
                    if(result != null) builder.append(result.getClass().getSimpleName() + " : ");
                    builder.append(toReadableString(result));
                    builder.append("\r\n");
                    if(exception != null) {
                        builder.append("Exception: \r\n");
                        StringWriter w = new StringWriter();
                        exception.printStackTrace(new PrintWriter(w));
                        builder.append(w);
                    }
                    builder.append("\r\n");
                    builder.append("Log: \r\n");
                    builder.append(SuperIO.readTopOfFile(tmpLogFile.getAbsolutePath(), 10000));
                    builder.append("\r\n");
                    log.debug("Sending mail...");
                    try {
// TODO                       sendMail(mailSmtpServer, mailFrom, mailTo, mailTitle, builder.toString());
                    } catch(Exception e) {
                        log.debug("error during mailing:" + e);
                    }
                    log.debug("...mail sent.");
                }
            }
        } catch(Throwable th){
            th.printStackTrace();
            PrintWriter pw = new PrintWriter(new File("/tmp/ErrorLoggerService.error." + System.currentTimeMillis() + "." + GuidFactory.getInstance().makeGuid()));
            th.printStackTrace(pw);
            pw.close();
        }
        tmpLogFile.delete();
        if(exception != null) throw exception;
        //timer.tmpTime("19:");
        return result;
    }

    private String toReadableString(Object object) {
        if(object == null) {
            return "null";
        } else if(object instanceof ModelObject) {
            return ((ModelObject) object).toDebugString("; ");
        } else if(object instanceof Object[]) {
            return Arrays.toString((Object[]) object);
        } else {
           return object.toString();
        }
    }

    private class SameThreadStringWriter extends FileWriter {
        private final Thread originalThread;


        public SameThreadStringWriter(File tmpLogFile) throws IOException {
            super(tmpLogFile);
            this.originalThread = Thread.currentThread();
        }

        public void write(char cbuf[]) throws IOException {
            if(Thread.currentThread() == originalThread) super.write(cbuf);
        }

        public void write(int c) throws IOException {
            if(Thread.currentThread() == originalThread) super.write(c);
        }

        public void write(char cbuf[], int off, int len) throws IOException {
            if(Thread.currentThread() == originalThread) super.write(cbuf, off, len);
        }

        public void write(String str) throws IOException {
            if(Thread.currentThread() == originalThread) super.write(str);
        }

        public void write(String str, int off, int len) throws IOException {
            if(Thread.currentThread() == originalThread) super.write(str, off, len);
        }

        public Writer append(CharSequence csq) throws IOException {
            if(Thread.currentThread() == originalThread) return super.append(csq);
            return this;
        }

        public Writer append(CharSequence csq, int start, int end) throws IOException {
            if(Thread.currentThread() == originalThread) return super.append(csq, start, end);
            return this;
        }

        public Writer append(char c) throws IOException {
            if(Thread.currentThread() == originalThread) return super.append(c);
            return this;
        }
    }

    private class InterestingFilter extends Filter {
        private final Thread originalThread;

        public InterestingFilter() {
            this.originalThread = Thread.currentThread();
        }

        public int decide(LoggingEvent event) {
            if(Thread.currentThread() == originalThread && event.getLevel().toInt() > maxLevel) {
                maxLevel = event.getLevel().toInt();
            }
            return NEUTRAL;
        }

        private int maxLevel;
    }



    public static void main(String[] args) {
        Map<String, String> map = System.getenv();
        StringBuilder b = new StringBuilder();

        for(Iterator<Map.Entry<String, String>> entryIterator = map.entrySet().iterator(); entryIterator.hasNext(); ){
            Map.Entry<String, String> stringStringEntry = entryIterator.next();
            b.append(stringStringEntry.getKey() + " = " + stringStringEntry.getValue() + "\n");
        }
        System.out.println(b);

        System.out.println("---------------------------------------");
        System.out.println("System.getenv(\"USER\") = " + System.getenv("USER"));
        System.out.println("System.getenv(\"HOST\") = " + System.getenv("HOST"));
        System.out.println("System.getenv(\"HOSTNAME\") = " + System.getenv("HOSTNAME"));
        System.out.println("System.getenv(\"LOGNAME\") = " + System.getenv("LOGNAME"));
        System.out.println("System.getenv(\"PWD\") = " + System.getenv("PWD"));

    }

}
