package dk.lessismore.nojpa.db;

import dk.lessismore.nojpa.db.connectionpool.ConnectionPoolFactory;
import dk.lessismore.nojpa.db.statements.InsertSQLStatement;
import dk.lessismore.nojpa.db.statements.PreparedSQLStatement;
import dk.lessismore.nojpa.db.statements.SelectSQLStatement;
import dk.lessismore.nojpa.db.statements.mysql.MySqlPreparedSQLStatement;
import dk.lessismore.nojpa.db.statements.mysql.MySqlUtil;
import dk.lessismore.nojpa.resources.PropertyResources;
import dk.lessismore.nojpa.resources.PropertyService;
import dk.lessismore.nojpa.utils.EventCounter;
import dk.lessismore.nojpa.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * This class can execute an sql statement. It uses a connection pool of mysql connections.
 * If you want the sql statements can allso be redirected to an sql file, instead of
 * being executed.
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public class SQLStatementExecutor {




    private static Logger log = LoggerFactory.getLogger(SQLStatementExecutor.class);
    public static boolean debugMode = false;
    public static boolean debugCpuMode = false; //true  = memory leak
    private static EventCounter eventCounter = new EventCounter();

    static long totalCounter = 0;
    static long totalTime = 0;



    static {
        log = LoggerFactory.getLogger(SQLStatementExecutor.class);
        PropertyResources websiteResources = PropertyService.getInstance().getPropertyResources(SQLStatementExecutor.class);
        if (websiteResources.getString("debug") != null && websiteResources.getBoolean("debug")) {
            debugMode = true;
        } else {
            debugMode = false;
        }
        try{
            SQLStatementExecutor.doQuery("select 1+1;");
        } catch (Exception e){
            System.out.println("Some ERROR when warming up.... " + e);
            e.printStackTrace();
        }
    }



    public static EventCounter getEventCounter(){ return eventCounter; }

    private static void close(AutoCloseable... autoCloseables) throws Exception {
        for(AutoCloseable a : autoCloseables){
            if(a != null){ a.close(); }
        }
    }

    public static boolean doUpdate(InsertSQLStatement insertSQLStatement) {
        return doUpdate(insertSQLStatement, false);
    }

    public static boolean doUpdate(InsertSQLStatement insertSQLStatement, boolean containsLob) {
        if(debugMode && !containsLob){
            return doUpdate(insertSQLStatement.makeStatement());
        } else {
            Connection connection = null;
            PreparedStatement statement = null;
            try {
                PreparedSQLStatement preSQLStatement = new MySqlPreparedSQLStatement();
                String initStatement = insertSQLStatement.makePreparedStatement(preSQLStatement);
                long start = System.currentTimeMillis();
                connection = (Connection) ConnectionPoolFactory.getPool().getFromPool();
                statement = connection.prepareStatement(initStatement);

                Iterator<Map.Entry<String, Pair<Object, Class>>> iterator = insertSQLStatement.getAttributeValuesAndTypes().entrySet().iterator();
                for(int i = 1; iterator.hasNext(); i++){
                    Map.Entry<String, Pair<Object, Class>> next = iterator.next();
                    if(next.getValue().getSecond().equals(Integer.class)){
                        statement.setInt(i, (Integer) next.getValue().getFirst());
                    } else if(next.getValue().getSecond().equals(Double.class)){
                        statement.setDouble(i, (Double) next.getValue().getFirst());
                    } else if(next.getValue().getSecond().equals(Float.class)){
                        statement.setFloat(i, (Float) next.getValue().getFirst());
                    } else if(next.getValue().getSecond().equals(Long.class)){
                        statement.setLong(i, (Long) next.getValue().getFirst());
                    } else if(next.getValue().getSecond().equals(Boolean.class)){
                        statement.setBoolean(i, (Boolean) next.getValue().getFirst());
                    } else if(next.getValue().getSecond().equals(Calendar.class)){
                        statement.setTimestamp(i, new java.sql.Timestamp(((Calendar) next.getValue().getFirst()).getTimeInMillis()), (Calendar) next.getValue().getFirst());
                    } else if(next.getValue().getSecond().equals(String.class)){
                        statement.setString(i, (String) next.getValue().getFirst());
                    } else {
                        log.error("Don't know what to do with type("+ next.getValue().getSecond() +")->("+ next.getValue().getFirst() +") with insert-statement("+ initStatement +")");
                    }
                }

                long end = System.currentTimeMillis();
                long time = end - start;
                totalTime = totalTime + time;

                log.debug("doUpdate:Time("+ (time) +")::" + initStatement);

                statement.executeUpdate();

                totalCounter++;
                if(debugCpuMode) {
                    eventCounter.newEvent(initStatement, end - start);
                    eventCounter.newEvent("insert", end - start);
                }
                ConnectionPoolFactory.getPool().putBackInPool(connection);
                return true;
            } catch (Exception e) {
                log.error("Update/Insert sql execution failed 2nd try (GIVING UP) \nstmt=" + insertSQLStatement, e);
                try {
                    close(statement, connection);
                    ConnectionPoolFactory.getPool().addNew();
                } catch (Exception exp) {
                    log.warn("Trying to close connection because of error ..." + exp.toString());
                }
                return false;
            } finally {
                try {
                    close(statement);
                } catch (Exception ex) {
                    //Nothing
                }
            }
        }
    }


    public static boolean doUpdate(String sqlStatement) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = (Connection) ConnectionPoolFactory.getPool().getFromPool();

            long start = System.currentTimeMillis();
            statement = connection.createStatement();
            statement.execute(sqlStatement);
            long end = System.currentTimeMillis();
            long time = end - start;
            log.debug("doUpdate:Time("+ time +")::" + sqlStatement.replaceAll("\n", " "));
            totalTime = totalTime + time;
            totalCounter++;
            if(debugCpuMode) {
                eventCounter.newEvent(sqlStatement.replaceAll("\n", " "), end - start);
                eventCounter.newEvent("insert", end - start);
            }
            ConnectionPoolFactory.getPool().putBackInPool(connection);
            return true;
        } catch (Exception e) {
            log.warn("Update/Insert sql execution failed (will try to recover) \nstmt=" + sqlStatement, e);
            try {
                close(statement, connection);
                ConnectionPoolFactory.getPool().addNew();
            } catch (Exception ex) {
                log.warn("Trying to close connection because of error ..." + ex.toString());
            }
            connection = (Connection) ConnectionPoolFactory.getPool().getFromPool();
            try {
                statement = connection.createStatement();
                statement.execute(sqlStatement);
                ConnectionPoolFactory.getPool().putBackInPool(connection);
                return true;
            } catch (Exception ex) {
                log.error("Update/Insert sql execution failed 2nd try (GIVING UP) \nstmt=" + sqlStatement, e);
                try {
                    close(statement, connection);
                    ConnectionPoolFactory.getPool().addNew();
                } catch (Exception exp) {
                    log.warn("Trying to close connection because of error ..." + exp.toString());
                }
                return false;
            }
        } finally {
            try {
                close(statement);
            } catch (Exception ex) {
                //Nothing
            }
        }
    }



    public static LimResultSet doQuery(String sqlStatement) {
        Statement statement = null;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = (Connection) ConnectionPoolFactory.getPool().getFromPool();
            long start = System.currentTimeMillis();
            statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            log.debug("doQuery::Will run: " + sqlStatement.replaceAll("\n", " "));

            resultSet = statement.executeQuery(sqlStatement);
            long end = System.currentTimeMillis();
            long time = end - start;
            log.debug("doQuery::Time("+ time +") " + sqlStatement.replaceAll("\n", " "));
            totalTime = totalTime + time;
            totalCounter++;
//                log.debug("**************** AVG-TIME("+ (totalTime / totalCounter) +") count("+ totalCounter +") totalTime("+ totalTime +") lastTime("+ time +")");
            if(debugCpuMode) eventCounter.newEvent(sqlStatement.replaceAll("\n", " "), end - start);
            LimResultSet toReturn = new LimResultSet(resultSet, statement, sqlStatement);
            ConnectionPoolFactory.getPool().putBackInPool(connection);
//                log.debug("ZZZZZZZZZZ toReturn("+ toReturn +"), resultSet("+ resultSet +"), toReturn("+ toReturn +"), toReturn.getResultSet("+ ( toReturn != null ? toReturn.getResultSet() : null ) +")");
            return toReturn;
        } catch (Exception e) {
            log.error("query sql execution failed \nstmt=" + sqlStatement, e);
            try {
                close(resultSet, statement, connection);
                ConnectionPoolFactory.getPool().addNew();
            } catch (Exception ex) {
                log.error("2:Trying to close connection because of error ..." + ex.toString());
            }
            try {
                connection = (Connection) ConnectionPoolFactory.getPool().getFromPool();
                statement = connection.createStatement();
                resultSet = statement.executeQuery(sqlStatement);
                LimResultSet toReturn = new LimResultSet(resultSet, statement, sqlStatement);
                ConnectionPoolFactory.getPool().putBackInPool(connection);
                return toReturn;
            } catch (Exception ex) {
                log.error("Some error in doQuery " + e.toString());
                try {
                    close(resultSet, statement, connection);
                    ConnectionPoolFactory.getPool().addNew();
                } catch (Exception exp) {
                    log.warn("2:Trying to close connection because of error ..." + exp.toString());
                }
            }
        }

        return null;
    }


    public static LimResultSet doQuery(SelectSQLStatement selectSQLStatement) {
        if(debugMode){
            return doQuery("" + selectSQLStatement);
        } else {
            PreparedStatement statement = null;
            Connection connection = null;
            ResultSet resultSet = null;
            try {
                PreparedSQLStatement preSQLStatement = new MySqlPreparedSQLStatement();
                String initStatement = selectSQLStatement.makePreparedStatement(preSQLStatement);
                connection = (Connection) ConnectionPoolFactory.getPool().getFromPool();
                long start = System.currentTimeMillis();
                statement = connection.prepareStatement(initStatement, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                preSQLStatement.makeStatementReadyToExcute(statement);
                log.debug("doQuery::Will run" + initStatement.replaceAll("\n", " "));

                resultSet = statement.executeQuery();
                long end = System.currentTimeMillis();
                long time = end - start;
                log.debug("doQuery::Time("+ time +") " + initStatement.replaceAll("\n", " "));
                totalTime = totalTime + time;
                totalCounter++;
//                log.debug("**************** AVG-TIME("+ (totalTime / totalCounter) +") count("+ totalCounter +") totalTime("+ totalTime +") lastTime("+ time +")");

                if(debugCpuMode) eventCounter.newEvent(initStatement.replaceAll("\n", " "), end - start);
                log.debug("Time("+ (end - start) +") for " + initStatement.replaceAll("\n", " "));
                LimResultSet toReturn = new LimResultSet(resultSet, statement, initStatement);
                ConnectionPoolFactory.getPool().putBackInPool(connection);
                return toReturn;
            } catch (Exception e) {
                log.warn("doQuery: query sql execution failed. We will try again: " + e, e);
                try {
                    close(statement, connection);
                    ConnectionPoolFactory.getPool().addNew();
                } catch (Exception ex) {
                    log.warn("2:Trying to close connection because of error ..." + ex.toString());
                }
                try {
                    PreparedSQLStatement preSQLStatement = new MySqlPreparedSQLStatement();
                    String initStatement = selectSQLStatement.makePreparedStatement(preSQLStatement);
                    connection = (Connection) ConnectionPoolFactory.getPool().getFromPool();
                    statement = connection.prepareStatement(initStatement, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    log.debug("doQuery: Will run (AGAIN): " + initStatement.replaceAll("\n", " "));
                    preSQLStatement.makeStatementReadyToExcute(statement);
                    resultSet = statement.executeQuery();
                    LimResultSet toReturn = new LimResultSet(resultSet, statement, initStatement);
                    ConnectionPoolFactory.getPool().putBackInPool(connection);
                    return toReturn;
                } catch (Exception exp) {
                    log.error("doQuery: query sql execution failed (GIVING-UP-1):" + e, e);
                    log.error("doQuery: query sql execution failed (GIVING-UP-2):" + exp, exp);
                    try {
                        close(statement, connection);
                        ConnectionPoolFactory.getPool().addNew();
                    } catch (Exception ex) {
                        log.warn("2:Trying to close connection because of error ..." + ex.toString());
                    }

                }

            }
            return null;
        }
    }




    static long start = 0L;

    public static void print(String str) {
        log.debug(str + " " + (System.currentTimeMillis() - start));
    }

    public static void printOutCpuStats(){
        List<EventCounter.Event> status = SQLStatementExecutor.getEventCounter().getStatus();
        for(int i = 0; i < status.size() && i < 200; i++){
            log.debug(status.get(i).countOfEvents + " \t " + status.get(i).totalTime + " \t " + status.get(i).key);
        } 
    }


    public static void main(String[] args) throws Exception {
        start = System.currentTimeMillis();
        for(int j = 0; j < 10; j++){
            long microStart = System.currentTimeMillis();
            print("START " + j);
            LimResultSet s = doQuery("select * from _Order where creationDate > '2006-04-01'");
            ResultSet resultSet = s.getResultSet();
            for(int i = 0; resultSet.next(); i++) {
              resultSet.getString("number");
            }
            print("END " + (System.currentTimeMillis() - microStart)+" " +  j);
        }
        print("ENDS ");


    }


    //    /* is used for alter index's - which properly will give a exception, because index already exists */
//    public static boolean doUpdateAndIgnoreExceptions(String sqlStatement) {
//        Connection connection = null;
//        Statement statement = null;
//        try {
//            connection = (Connection) ConnectionPoolFactory.getPool().getFromPool();
//            long start = System.currentTimeMillis();
//            statement = connection.createStatement();
//            log.debug("Will update with:" + sqlStatement.replaceAll("\n", " "));
//            statement.execute(sqlStatement);
//
//            long end = System.currentTimeMillis();
//            long time = end - start;
//            totalTime = totalTime + time;
//            totalCounter++;
////                log.debug("**************** AVG-TIME("+ (totalTime / totalCounter) +") count("+ totalCounter +") totalTime("+ totalTime +") lastTime("+ time +")");
//
//
//            if(debugCpuMode) {
//                eventCounter.newEvent(sqlStatement.replaceAll("\n", " "), end - start);
//                eventCounter.newEvent("insert", end - start);
//            }
//            statement.close();
//            ConnectionPoolFactory.getPool().putBackInPool(connection);
//            return true;
//
//        } catch (Exception e) {
//            return true;
//        } finally {
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//                statement = null;
//            } catch (Exception ex) {
//                //Nothing
//            }
//        }
//
//    }


}
