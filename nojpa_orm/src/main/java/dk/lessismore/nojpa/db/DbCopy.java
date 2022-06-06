package dk.lessismore.nojpa.db;


import org.apache.commons.lang3.StringUtils;

import javax.swing.plaf.IconUIResource;
import java.sql.*;
import java.util.*;

/**
 * This class can execute an sql statement. It uses a connection pool of mysql connections.
 * If you want the sql statements can allso be redirected to an sql file, instead of
 * being executed.
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public class DbCopy {

    static String dbFrom = null;
    static String dbFromHost = null;
    static String dbTo = null;
    static String dbToHost = null;
    static String pass = null;

    public static void main(String[] args) throws Exception {
        if (args != null && args.length > 0) {
            dbFrom = args[0];
            dbFromHost = args[1];
            dbTo  = args[2];
            dbToHost  = args[3];
            pass = args[4];
            Class.forName("com.mysql.jdbc.Driver");
            List<String> tables = getTables();
            for(String s : tables) {
                copyTable(s);
            }
        }
    }

    static Connection conFrom = null;
    private static Connection makeFromConnection() throws SQLException {
        System.out.println("makeFromConnection:1");
        if (conFrom == null) {
            conFrom = DriverManager.getConnection(
                    "jdbc:mysql://" + dbFromHost + ":3306/" + dbFrom, "root", pass);
        }
        System.out.println("makeFromConnection:2");
        return conFrom;
    }

    static Connection conTo = null;
    private static Connection makeToConnection() throws SQLException {
        System.out.println("makeToConnection:1");
        if(conTo == null) {
            conTo = DriverManager.getConnection(
                    "jdbc:mysql://" + dbToHost + ":3306/" + dbTo, "root", pass);
        }
        System.out.println("makeToConnection:2");
        return conTo;
    }

    final static HashMap<String, Integer> countStar = new HashMap<>();
    public static List<String> getTables() throws SQLException {
        Connection connection = makeFromConnection();
        Statement statement = connection.createStatement();
        ResultSet tables = statement.executeQuery("show tables");
        List<String> toReturn = new ArrayList<>();
        while (tables.next()) {
            String name = tables.getString(1);
            System.out.println("getTables:" + name);
            toReturn.add(name);
        }
        tables.close();
        statement.close();
        System.out.println("getTables:COUNT-STAR");
        for(String table : toReturn) {
            Statement stm = connection.createStatement();
            ResultSet resultSet = stm.executeQuery("select count(*) from " + table);
            while(resultSet.next()) {
                System.out.println("getTables:COUNT-STAR:START:" + table);
                int anInt = resultSet.getInt(1);
                countStar.put(table, anInt);
                System.out.println("getTables:COUNT-STAR:END:" + table + " -> " + anInt);
            }
        }
        System.out.println("getTables:DONE");
        return toReturn;
    }

    static long totalTime = 0;
    static long totalRows = 0;
    public static void copyTable(String table) throws SQLException {
        long start = System.currentTimeMillis();
        System.out.println("copyTable:1");
        Connection connectionTo = makeToConnection();
        Statement statement = connectionTo.createStatement();
        System.out.println("copyTable:2");
        ResultSet resultSet = statement.executeQuery("select * from " + table + " limit 0,1");
        List<String> cls = new ArrayList<>();
        System.out.println("copyTable:3");
        ResultSetMetaData metaData = resultSet.getMetaData();
        System.out.println("copyTable:4");
        int columnCount = metaData.getColumnCount();
        for(int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            cls.add(columnName);
        }
        resultSet.close();
        System.out.println("copyTable:5");
        int targetCountStar = 0;
        {
            Statement stm = connectionTo.createStatement();
            ResultSet countSet = stm.executeQuery("select count(*) from " + table);
            while(countSet.next()) {
                System.out.println("target:COUNT-STAR:START:" + table);
                targetCountStar = countSet.getInt(1);
                System.out.println("target:COUNT-STAR:END:" + table + " -> " + targetCountStar);
            }
        }
        Integer rows = countStar.get(table);
        if (rows != null && rows != 0 && rows != targetCountStar) {
            String colNames = StringUtils.join(cls, ", ");
            String sqlDelete = "delete from " + table;
            String sql = "insert into " + table + " (" + colNames + ") select " + colNames + " from " + dbFrom + "." + table;
            System.out.println("Will delete table: ("+ sql +")");
            int delete = statement.executeUpdate(sqlDelete);
            statement.executeUpdate("ALTER TABLE "+ table +" DISABLE KEYS");

            if (rows != null && rows != 0 && totalRows != 0 && totalTime != 0) {
                Calendar now = Calendar.getInstance();
                Calendar future = Calendar.getInstance();
                future.add(Calendar.MILLISECOND, (int) ((((double) totalTime) / ((double) totalRows)) * rows));
                System.out.println("Now("+ now.getTime() +") Expect-to-be-done("+ future.getTime() +") with rows("+ rows +")");
            }
            System.out.println("Will execute: ("+ sql +")");
            int i = statement.executeUpdate(sql);
            System.out.println("Got result : " + i);
            statement.executeUpdate("ALTER TABLE "+ table +" ENABLE KEYS");
            System.out.println("Enabled keys.... Now optimizing : " + table);
            statement.executeUpdate("OPTIMIZE TABLE "+ table);
            System.out.println("Totally done with " + table);
            long end = System.currentTimeMillis();
            if (rows != null && rows != 0) {
                totalRows += rows;
                totalTime += (end - start);
            }
        }
    }


}
