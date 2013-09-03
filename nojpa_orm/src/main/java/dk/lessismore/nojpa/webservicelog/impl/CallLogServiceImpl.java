package dk.lessismore.nojpa.webservicelog.impl;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.file.Files;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import org.apache.log4j.Logger;
import dk.lessismore.nojpa.reflection.db.DbObjectSelector;
import dk.lessismore.nojpa.reflection.db.statements.SelectSqlStatementCreator;
import dk.lessismore.nojpa.db.statements.SelectSQLStatement;
import dk.lessismore.nojpa.db.statements.WhereSQLStatement;
import dk.lessismore.nojpa.db.LimResultSet;
import dk.lessismore.nojpa.db.SQLStatementExecutor;
import dk.lessismore.nojpa.webservicelog.CallerIdentifierLog;
import dk.lessismore.nojpa.webservicelog.CallLog;
import dk.lessismore.nojpa.webservicelog.CallLogService;

import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.io.File;

/**
 * Retrieves information about method calls that has happened in the system.
 */
// TODO: WARNING: This service has dynamically built queries and thus has potential
// for SQL injections. Although strings are stripped from most characters, a
// quick fix or a typo somewhere may be the thing that makes us vulnerable.
// Besides, this kind if custom quoting is not to be trusted.
// This service MUST NOT be available externally.
// It should be rewritten as plain reusable queries.
public class CallLogServiceImpl  implements CallLogService {

    private final static Logger log = org.apache.log4j.Logger.getLogger(CallLogServiceImpl.class);
    // TODO: It used to be CallLogImpl - does this work?
    private final static Class<CallLog> implementationClass = CallLog.class;
    private boolean interceptionEnabled = false;
    private static Set<Thread> threadsDisabled = Collections.synchronizedSet(new HashSet<Thread>());

    private static final File directory = Files.createTemporaryDirectory("webservicelog");

    public static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyyMMdd");
    }

    public static File getDirectory() {
        return directory;
    }

    private final static CallLogServiceImpl instance = new CallLogServiceImpl();

    /**
     * Calls the action without logging anything in the thread during the call.
     * If new threads are spawned, they will not be ignored; only the thread(s) currently calling ignore() will.
     */
    public synchronized void ignore(Runnable action) {
        if(!threadsDisabled.contains(Thread.currentThread())) {
            threadsDisabled.add(Thread.currentThread());
            try {
                action.run();
            } finally {
                threadsDisabled.remove(Thread.currentThread());
            }
        } else {
            action.run();
        }
    }

    public boolean isIgnoringThread(Thread thread) {
        //log.debug("isIgnoringThread: " + thread.toString() + "("+ thread.getId() +")   .... arrays:" + Arrays.toString(threadsDisabled.toArray()));
        return threadsDisabled.contains(thread);
    }

    public CallLog getCallLogByID(String id, CallerIdentifierLog callerIdentifierLog) {
        return MQL.selectByID(CallLog.class, id);
    }

    public CallLog[] getCallLogsByClass(
            String className, String methodName, int offset, int limit, int minMaxLoLevel,
            Date fromDateTime, Date toDateTime, CallerIdentifierLog callerIdentifierLog) {
        if(className != null) className = className.replaceAll(identifierRegex, "");
        SelectSqlStatementCreator selectSqlStatementCreator = new SelectSqlStatementCreator();
        selectSqlStatementCreator.setSource(implementationClass);
        SelectSQLStatement selectSQLStatement = selectSqlStatementCreator.getSelectSQLStatement();
        if(className != null) selectSqlStatementCreator.addConstrain(implementationClass, "className", WhereSQLStatement.EQUAL, className);
        selectSqlStatementCreator.addConstrain(implementationClass, "maxLogLevel", WhereSQLStatement.EQUAL_OR_GREATER, minMaxLoLevel);
        if (fromDateTime != null) {
            selectSqlStatementCreator.addConstrain(implementationClass, "creationDate", WhereSQLStatement.EQUAL_OR_GREATER, (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(fromDateTime));
        }
        if (toDateTime != null) {
            selectSqlStatementCreator.addConstrain(implementationClass, "creationDate", WhereSQLStatement.EQUAL_OR_LESS, (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(toDateTime));
        }
        if(methodName != null) {
            methodName = methodName.replaceAll(identifierRegex, "");
            selectSqlStatementCreator.addConstrain(implementationClass, "methodName", WhereSQLStatement.EQUAL, methodName);
        }
        selectSqlStatementCreator.addLimit(offset, offset + limit);
        selectSQLStatement.setOrderBy("lastModified", SelectSQLStatement.DESC);
        List result = DbObjectSelector.selectObjectsFromDb(implementationClass, selectSQLStatement);
        return (CallLog[]) result.toArray(new CallLog[result.size()]);
    }

    public CallLog[] getCallLogs(int offset, int limit, int minMaxLoLevel, Date fromDateTime, Date toDateTime, 
                                         CallerIdentifierLog callerIdentifierLog) {
        SelectSqlStatementCreator statementCreator = new SelectSqlStatementCreator();
        statementCreator.setSource(implementationClass);
        SelectSQLStatement statement = statementCreator.getSelectSQLStatement();
        statementCreator.addConstrain(implementationClass, "maxLogLevel", WhereSQLStatement.EQUAL_OR_GREATER, minMaxLoLevel);
        if (fromDateTime != null) {
            statementCreator.addConstrain(implementationClass, "creationDate", WhereSQLStatement.EQUAL_OR_GREATER, (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(fromDateTime));
        }
        statementCreator.addLimit(offset, offset + limit);
        statement.setOrderBy("lastModified", SelectSQLStatement.DESC);
        List result = DbObjectSelector.selectObjectsFromDb(implementationClass, statement);
        return (CallLog[]) result.toArray(new CallLog[result.size()]);
    }

    public CallLogMethod[] getCallLogMethods(int offset, int limit, int minMaxLogLevel, Date fromDateTime,
                                             Date toDateTime, CallerIdentifierLog callerIdentifierLog) {
        String whereString = "";
        if (fromDateTime != null) {
            whereString = "WHERE creationDate >= '" + (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(fromDateTime) + "' ";
        }
        String limitString = "";
        if(limit >= 0) {
            if(offset >= 0) {
                limitString = "LIMIT " + offset + ", " + limit;
            } else {
                limitString = "LIMIT " + limit;
            }
        }
        LimResultSet limSet = null;
        String query = "SELECT AVG(milliTime), MAX(milliTime), MAX(maxLogLevel) maxLogLevel, AVG(failed), COUNT(*), " +
                               "className, methodName " +
                        "FROM _CallLog " +
                        whereString +
                        "GROUP BY className, methodName " +
                        "HAVING maxLogLevel >= " + minMaxLogLevel + " " +
                        "ORDER BY className, methodName ASC " + 
                        limitString;
        try {
            limSet = SQLStatementExecutor.doQuery(query);
            if(limSet == null) return new CallLogMethod[0];
            ResultSet resultSet = limSet.getResultSet();
            List<CallLogMethod> methods = new ArrayList<CallLogMethod>();
            while(resultSet.next()) {
                CallLogMethod method = new CallLogMethod();
                method.averageMilliTime = resultSet.getLong(1);
                method.maxMilliTime = resultSet.getLong(2);
                method.maxLevel = resultSet.getInt(3);
                method.averageFailures = resultSet.getDouble(4);
                method.count = resultSet.getInt(5);
                method.className = resultSet.getString(6);
                method.methodName = resultSet.getString(7);
                methods.add(method);
            }
            resultSet.close();
            return methods.toArray(new CallLogMethod[methods.size()]);
        } catch(SQLException exception) {
            log.error("getCallLogMethods :: Some error " + exception);
            exception.printStackTrace();
            return null;
        } finally {
            if (limSet != null) limSet.close();
        }
    }

    public CallLogMethod[] getPerformance(int offset, int limit, CallerIdentifierLog callerIdentifierLog) {
        String limitString = "";
        if(limit >= 0) {
            if(offset >= 0) {
                limitString = "LIMIT " + offset + ", " + limit;
            } else {
                limitString = "LIMIT " + limit;
            }
        }
        LimResultSet limSet = null;
        String query = "SELECT SUM(milliTime), AVG(milliTime), MAX(milliTime), COUNT(*), className, methodName " +
                        "FROM _CallLog " +
                        "GROUP BY className, methodName " +
                        "ORDER BY SUM(milliTime) DESC " +
                        limitString
                ;
        try {
            limSet = SQLStatementExecutor.doQuery(query);
            if(limSet == null) return new CallLogMethod[0];
            ResultSet resultSet = limSet.getResultSet();
            List<CallLogMethod> methods = new ArrayList<CallLogMethod>();
            while(resultSet.next()) {
                CallLogMethod method = new CallLogMethod();
                method.totalMilli = resultSet.getLong(1);
                method.averageMilliTime = resultSet.getLong(2);
                method.maxMilliTime = resultSet.getLong(3);
                method.count = resultSet.getInt(4);
                method.className = resultSet.getString(5);
                method.methodName = resultSet.getString(6);
                methods.add(method);
            }
            resultSet.close();
            return methods.toArray(new CallLogMethod[methods.size()]);
        } catch(SQLException exception) {
            log.error("getPerformance :: Some error " + exception);
            exception.printStackTrace();
            return null;
        } finally {
            if (limSet != null) limSet.close();
        }
    }

    public CallLogMethod getCallLogMethodLastCalls(
            String className, String methodName,
            int callCount, Date fromDateTime, Date toDateTime,
            int minMaxLogLevel,
            CallerIdentifierLog callerIdentifierLog) {
        if(className != null) className = className.replaceAll(identifierRegex, "");
        if(methodName != null) methodName = methodName.replaceAll(identifierRegex, "");
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String from = fromDateTime == null ? "" : " AND lastModified > '" + format.format(fromDateTime) + "' ";
        String to = toDateTime == null ? "" : " AND lastModified < '" + format.format(toDateTime) + "' ";
        LimResultSet limSet = null;
        try {
            limSet = SQLStatementExecutor.doQuery(
                    "SELECT AVG(t.milliTime), MAX(t.milliTime), MAX(t.maxLogLevel), AVG(t.failed), " +
                            "COUNT(t.milliTime) " +
                            "FROM (SELECT * FROM _CallLog WHERE 1 = 1 " +
                            (className != null ? " AND className='" + className + "' " : "") +
                            (methodName != null ? " AND methodName='" + methodName + "' " : "") +
                            (" AND maxLogLevel>='" + minMaxLogLevel + "' ") +
                            from + to +
                            (callCount != -1 ? " ORDER BY lastModified DESC LIMIT " + callCount : "") + ") as t");
            if(limSet == null) return null;
            ResultSet resultSet = limSet.getResultSet();
            CallLogMethod method = new CallLogMethod();
            method.className = className;
            method.methodName = methodName;
            if(resultSet.next()) {
                method.averageMilliTime = resultSet.getLong(1);
                method.maxMilliTime = resultSet.getLong(2);
                method.maxLevel = resultSet.getInt(3);
                method.averageFailures = resultSet.getDouble(4);
                method.count = resultSet.getInt(5);
            }
            resultSet.close();
            return method;
        } catch(SQLException exception) {
            log.error("getCallLogMethods :: Some error " + exception);
            exception.printStackTrace();
            return null;
        } finally {
            if (limSet != null) limSet.close();
        }
    }

    /*
    public CallLogMethod[] getCallLogMethodSinceFlushWithProblems(
            double failureRateThreshold, int minimumCalls,
            int minMaxLogLevel,
            CallerIdentifierLog callerIdentifierLog) {
        List<CallLogMethod> stats = new ArrayList<CallLogMethod>();
        Class<CallLogFlushImpl> flushClass = CallLogFlushImpl.class;
        for(CallLogMethod classInfo: getCallLogMethods(-1, -1, minMaxLogLevel, null, null, callerIdentifierLog)) {
            SelectSqlStatementCreator selectSqlStatementCreator = new SelectSqlStatementCreator();
            selectSqlStatementCreator.setSource(flushClass);
            SelectSQLStatement selectSQLStatement = selectSqlStatementCreator.getSelectSQLStatement();
            selectSqlStatementCreator.addConstrain(flushClass, "className", WhereSQLStatement.EQUAL, classInfo.className);
            List<CallLogFlushImpl> result = DbObjectSelector.selectObjectsFromDb(flushClass, selectSQLStatement);
            CallLogFlushImpl[] flushes = result.toArray(new CallLogFlushImpl[result.size()]);
            CallLogFlushImpl flush = flushes.length != 0 ? flushes[0] : null;
            if(flush == null) {
                flush = new CallLogFlushImpl();
                flush.setLastFlush(0);
                flush.setClassName(classInfo.className);
            }
            Date since = new Date(flush.getLastFlush());
            Date now = new Date();
            CallLogMethod stat = getCallLogMethodLastCalls(
                    classInfo.className, null, -1, since, null, minMaxLogLevel, callerIdentifierLog);
            if(stat.count >= minimumCalls && stat.averageFailures > failureRateThreshold) {
                stats.add(stat);
                flush.setLastFlush(now.getTime());
                flush.save();
            }
        }
        return stats.toArray(new CallLogMethod[stats.size()]);
    }
    */

    public CallLog[] getCallLogByParent(
            CallLog parent, int offset, int limit, int minMaxLogLevel,
            Date fromDateTime, Date toDateTime, CallerIdentifierLog callerIdentifierLog) {
        SelectSqlStatementCreator selectSqlStatementCreator = new SelectSqlStatementCreator();
        selectSqlStatementCreator.setSource(implementationClass);
        SelectSQLStatement selectSQLStatement = selectSqlStatementCreator.getSelectSQLStatement();
        selectSqlStatementCreator.addLimit(offset, offset + limit);
        selectSqlStatementCreator.addConstrain(implementationClass, "maxLogLevel", WhereSQLStatement.EQUAL_OR_GREATER, minMaxLogLevel);
        if (fromDateTime != null) {
            selectSqlStatementCreator.addConstrain(implementationClass, "creationDate", WhereSQLStatement.EQUAL_OR_GREATER, (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(fromDateTime));
        }
        if (toDateTime != null) {
            selectSqlStatementCreator.addConstrain(implementationClass, "creationDate", WhereSQLStatement.EQUAL_OR_LESS, (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(toDateTime));
        }
        if(parent == null) {
            selectSqlStatementCreator.addConstrain(implementationClass, "parent", WhereSQLStatement.EQUAL, (String) null);
        } else {
            selectSqlStatementCreator.addConstrain(implementationClass, "parent", WhereSQLStatement.EQUAL, "" + parent);
        }
        selectSQLStatement.setOrderBy("lastModified", SelectSQLStatement.DESC);
        List<CallLog> result = DbObjectSelector.selectObjectsFromDb(implementationClass, selectSQLStatement);
        CallLog[] calls = result.toArray(new CallLog[result.size()]);
        return calls.length != 0 ? calls : null;
    }

    public int removeCallLogsFromBefore(Calendar calendar, CallerIdentifierLog callerIdentifierLog) {
        for(File dir: directory.listFiles()) {
            try {
                Date date = (new SimpleDateFormat("yyyyMMdd")).parse(dir.getName());
                GregorianCalendar fileCalendar = new GregorianCalendar();
                fileCalendar.setTime(date);
                File[] files = dir.listFiles();
                if(files != null) {
                    if(fileCalendar.before(calendar)) {
                        for(File file: files) {
                            file.delete();
                        }
                        dir.delete();
                    } else {
                        log.debug("Skipping directory " + dir);
                    }
                } else {
                    log.debug("Skipping file " + dir);
                }
            } catch (ParseException e) {
                // Skip this directory
                log.debug("Skipping directory " + dir);
            }
        }
        int count = 0;
        while(true) {
            CallLog mock = MQL.mock(CallLog.class);
            List<CallLog> result = MQL.select(mock).where(mock.getCreationDate(), MQL.Comp.LESS, calendar).getList();
            if(result.isEmpty()) return count;
            for(CallLog log: result) {
                ModelObjectService.delete(log);
            }
            count += result.size();
        }
    }

    public boolean getInterceptionEnabled() {
        return interceptionEnabled;
    }

    public void setInterceptionEnabled(boolean enabled) {
        interceptionEnabled = enabled;
    }

    public static CallLogService getInstance() {
        return instance;
    }

    public static class CallLogMethod {
        public String methodName;
        public String className;
        public int maxLevel;
        public long maxMilliTime;
        public long averageMilliTime;
        public long totalMilli;
        public double averageFailures;
        public int count;
    }

    private static final String identifierRegex = "[^A-Za-z0-9.$:_]";

     public String fetchArguments(CallLog callLog) {
        return readFile(callLog, ".arguments");
    }

    public String fetchResult(CallLog callLog) {
        return readFile(callLog, ".result");
    }

    public String fetchLog(CallLog callLog) {
        return readFile(callLog, ".log");
    }

    public String fetchException(CallLog callLog) {
        return readFile(callLog, ".exception");
    }

    private String readFile(CallLog callLog, String fileName) {
        File file = new File(new File(directory, (new SimpleDateFormat("yyyyMMdd")).format(callLog.getLastModified().getTime())),
                callLog.getObjectID() + fileName);
        return Files.read(file);
    }
}
