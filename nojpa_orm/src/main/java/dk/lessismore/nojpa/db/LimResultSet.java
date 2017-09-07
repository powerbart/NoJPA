package dk.lessismore.nojpa.db;

import dk.lessismore.nojpa.guid.GuidFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public class LimResultSet {

    private static final Logger log = LoggerFactory.getLogger(LimResultSet.class);

    private final static Object toSync = new Object();

    private static long numberOfSetInTotal = 0;
    private static long numberOfSetLive = 0;
    private static long numberOfSetUnclosed = 0;
    private static final ArrayList<LimResultSet> listOfUnclosedSets = new ArrayList<LimResultSet>();
    private static Calendar reloadTime = Calendar.getInstance();

    private String objectID = GuidFactory.getInstance().makeGuid();
    private ResultSet resultSet = null;
    private Statement statement = null;
    private String rawSqlStatement = null;
    private boolean haveBeenClosed = false;
    private Calendar creationDate = Calendar.getInstance();
    private StackTraceElement[] stackTraceElements = null;


    public LimResultSet(ResultSet resultSet, Statement statement, String rawSqlStatement) {
        this.stackTraceElements = null; //SQLStatementExecutor.debugMode ? Thread.currentThread().getStackTrace() : null;
        this.resultSet = resultSet;
        this.statement = statement;
        this.rawSqlStatement = rawSqlStatement;
        synchronized (listOfUnclosedSets){
            ++numberOfSetInTotal;
            ++numberOfSetLive;
            ++numberOfSetUnclosed;
            listOfUnclosedSets.add(this);
        }
    }

    public ResultSet getResultSet() {
        return resultSet;
    }


    public synchronized void close() {
        try {
            if (statement != null) statement.close();
            if (resultSet != null) resultSet.close();
            resultSet = null;
            statement = null;
            synchronized (listOfUnclosedSets) {
                if (!haveBeenClosed) {
                    numberOfSetUnclosed--;
                    haveBeenClosed = true;
                    listOfUnclosedSets.remove(this);
                }
            }


            synchronized (listOfUnclosedSets) {
                if (numberOfSetUnclosed > 10) {
                    Calendar min2 = Calendar.getInstance();
                    min2.add(Calendar.MINUTE, -2);
                    for (int j = listOfUnclosedSets.size() - 1; j >= 0; j--) {
                        LimResultSet limResultSet = listOfUnclosedSets.get(j);
                        if(limResultSet.creationDate.before(min2)){
                            log.error("UNCLOSEDSETS(" + Thread.currentThread().getId() +")["+ (limResultSet.creationDate.getTime()) +"] : " + (limResultSet.rawSqlStatement.replaceAll("\n", "")));
                            for(int i = 0; stackTraceElements != null && i < stackTraceElements.length; i++){
                                log.error("UNCLOSEDSETS("+ Thread.currentThread().getId() +") : " + stackTraceElements[i].getClassName() + "." + stackTraceElements[i].getMethodName() + ":" + stackTraceElements[i].getLineNumber());

                            }
//                            Calendar min10 = Calendar.getInstance();
//                            min10.add(Calendar.MINUTE, -10);
//                            if(limResultSet.creationDate.before(min10)){
//                                try{
//                                    log.error("Closing " + limResultSet.rawSqlStatement);
//                                    limResultSet.close();
//                                } catch(Exception e){
//                                    e.printStackTrace();
//                                    log.error("Some error " + e, e);
//                                }
//                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            log.error("Some error in close() " + e, e);
            e.printStackTrace();
        }
    }

    protected void finalize() {
        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        synchronized (toSync) {
            --numberOfSetLive;
        }
    }

    public static long getNumberOfSetInTotal() {
        synchronized (toSync) {
            return numberOfSetInTotal;
        }
    }

    public static long getNumberOfSetLive() {
        synchronized (toSync) {
            return numberOfSetLive;
        }
    }

    public static long getNumberOfSetUnclosed() {
        synchronized (toSync) {
            return numberOfSetUnclosed;
        }
    }

    public static Calendar getReloadTime() {
        return reloadTime;
    }

    public static double selectsPerSecond() {
        Calendar now = Calendar.getInstance();
        long totalSec = (now.getTimeInMillis() - reloadTime.getTimeInMillis()) / 1000;
        return ((double) (numberOfSetInTotal)) / ((double) totalSec);
    }

    public static double selectsPerMinut() {
        return selectsPerSecond() * 60d;
    }
}
