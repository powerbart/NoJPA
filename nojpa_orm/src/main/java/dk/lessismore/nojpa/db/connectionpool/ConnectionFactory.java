package dk.lessismore.nojpa.db.connectionpool;

import dk.lessismore.nojpa.db.statements.SQLStatementFactory;
import dk.lessismore.nojpa.pool.factories.*;
import java.sql.*;
import java.util.Enumeration;

import dk.lessismore.nojpa.resources.*;
import org.apache.log4j.Logger;

/**
 * This class can make an instance of an database connection. The database properties
 * is defined in the property file called db.
 * <ul>
 * <li>ip: The location of the database.
 * <li>port: The port of the database.
 * <li>db: The database type/vendor (oracel, mysql etc)
 * <li>databaseName: The name of the database.
 * <li>driverName: The class path of the jdbc driver
 * <li>dbuser: The database user name.
 * <li>dbpasswd: The password of the database user.
 * </ul>
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public class ConnectionFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = Logger.getLogger(ConnectionFactory.class);
    private static Resources resources;

    private String ip = "localhost";
    private int port = 3306;
    private String dbName = "test";
    private String user = "";
    private String password = "";
    private String driverName = ""; //com.mysql.jdbc.Driver  "org.gjt.mm.mysql.Driver"; "com.mysql.jdbc.Driver";
    private String db = "mysql";
    private String dn = "some";

    private String poolName = null;

    public ConnectionFactory() {
        ConnectionFactory.resources = new PropertyResources("db");
        init();
    }
    public ConnectionFactory(Resources resources) {
        ConnectionFactory.resources = resources;
        init();
    }

    public ConnectionFactory(Resources resources, String poolName) {
        this.poolName = poolName;
        ConnectionFactory.resources = resources;
        init();
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getIp() {
        if(resources.gotResource("ip"))
            ip = resources.getString("ip");
        return ip;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public int getPort() {
        if(resources.isInt("port"))
            port = resources.getInt("port");
        return port;
    }

    public void setDbName(String dbName) {

        this.dbName = dbName;
    }
    public String getDbName() {
        if(poolName == null) {
            if(resources.gotResource("databaseName")) {
                dbName = resources.getString("databaseName");
            }
        } else {
            if(resources.gotResource(poolName +".databaseName")) {
                dbName = resources.getString(poolName + ".databaseName");
            }
        }
        return dbName;
    }

    public void setUser(String user) {
        this.user = user;
    }
    public String getUser() {
        if(poolName == null) {
            if(resources.gotResource("dbuser")) {
                user = resources.getString("dbuser");
            } else if (resources.gotResource("user")) {
                user = resources.getString("user");
            }
        } else {
            if(resources.gotResource(poolName +".dbuser")) {
                user = resources.getString(poolName +".dbuser");
            } else if (resources.gotResource(poolName +".user")) {
                user = resources.getString(poolName +".user");
            }
        }
        return user;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        if(poolName == null) {
            if(resources.gotResource("dbpasswd")) {
                password = resources.getString("dbpasswd");
            } else if (resources.gotResource("password")) {
                password = resources.getString("password");

            }
        } else {
            if(resources.gotResource(poolName +".dbpasswd")) {
                password = resources.getString(poolName +".dbpasswd");
            } else if (resources.gotResource(poolName +".password")) {
                password = resources.getString(poolName +".password");

            }
        }
        return password;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
    public String getDriverName() {
        if(poolName == null) {
            if (resources.gotResource("driverName")) {
                driverName = resources.getString("driverName");
            }
        } else {
            if (resources.gotResource(poolName +".driverName")) {
                driverName = resources.getString(poolName + ".driverName");
            }
        }
        return driverName;
    }

    public void setDb(String db) {
        this.db = db;
    }
    public String getDb() {
        if(resources.gotResource("database"))
            db = resources.getString("database");
        return db;
    }

    public boolean init() {
        try {
            Class.forName(getDriverName()).newInstance();
            return true;
        }catch(Exception e) {
            log.error("Could not make instance of db drivere " + driverName, e);
            return false;
        }
    }

    public Object makeResource() {
        String conStr = null;
        try {
//            log.debug("db.properties -- start -----");
//            for(Enumeration names = resources.getResourceNames(); names.hasMoreElements(); ){
//                String s = "" + names.nextElement();
//                log.debug("db.properties : " + s + " = " + resources.getResource(s));
//            }
//            log.debug("db.properties -- end ----- ::: (" + getDriverName() + ") getUser("+ getUser() +") res.get("+ resources.getResource("driverName")+") ..... GotIt? " + resources.gotResource("driverName"));

            //SQLStatementFactory.setDriverName(getDriverName());

            if(poolName == null){
                SQLStatementFactory.setDriverName(getDriverName());
            }


            if(getDriverName().contains("mysql")){
                Class.forName(getDriverName()).newInstance();
                conStr = "jdbc:"+getDb()+"://"+getIp()+":"+getPort()+"/"+getDbName();
                log.debug("Creating new MySQL-DB-Connection " + conStr);
                return DriverManager.getConnection(conStr, getUser(), getPassword());
            } else if(getDriverName().contains("h2")){
                Class.forName(getDriverName()).newInstance();
                conStr = "jdbc:"+getDb()+":"+getDbName();
                log.debug("Creating new H2-DB-Connection " + conStr);
                return DriverManager.getConnection(conStr, getUser(), getPassword());
            } else if(getDriverName().contains("oracle")){
                // "jdbc:oracle:thin:@ora_twd:1521/twd", "ccrd_gui", "ccrd_gui1234"
                return DriverManager.getConnection(getDbName(), getUser(), getPassword());
            } else {
                throw new RuntimeException("Unsupported database driver ");
            }
        } catch(Exception ex) {
            String msg = "Could not make db connection: "+conStr+" user="+getUser();
            log.error(msg+" pass="+getPassword(), ex);
            throw new RuntimeException(msg, ex);
        }
    }

    public void closeResource(Object resource) {
        if(resource instanceof Connection) {
            try {
                ((Connection)resource).close();
            }catch(Exception e) {
                log.error("Could not close db connection", e);
            }
        }
    }
}
