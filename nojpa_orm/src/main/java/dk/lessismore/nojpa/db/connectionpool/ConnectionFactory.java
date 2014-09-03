package dk.lessismore.nojpa.db.connectionpool;

import dk.lessismore.nojpa.pool.factories.*;
import java.sql.*;

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
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class ConnectionFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = Logger.getLogger(ConnectionFactory.class);
    private static Resources resources = new PropertyResources("db");

    private String ip = "localhost";
    private int port = 3306;
    private String dbName = "test";
    private String user = "";
    private String password = "";
    private String driverName = "com.mysql.jdbc.Driver"; //  "org.gjt.mm.mysql.Driver"; "com.mysql.jdbc.Driver";
    private String db = "mysql";

    public ConnectionFactory() {
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
        if(resources.gotResource("databaseName"))
            dbName = resources.getString("databaseName");

        return dbName;
    }

    public void setUser(String user) {
        this.user = user;
    }
    public String getUser() {
        if(resources.gotResource("dbuser")) {
            user = resources.getString("dbuser");
        } else if (resources.gotResource("user")) {
            user = resources.getString("user");
        }
        return user;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        if(resources.gotResource("dbpasswd")) {
            password = resources.getString("dbpasswd");
        } else if (resources.gotResource("password")) {
            password = resources.getString("password");

        }
        return password;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
    public String getDriverName() {
        if(resources.gotResource("driverName"))
            resources.getString("driverName");
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
            //log.debug("Making connection");
            Class.forName(getDriverName()).newInstance();
            conStr = "jdbc:"+getDb()+"://"+getIp()+"/"+getDbName();
            log.debug("Creating new DB-Connection " + conStr);
            return DriverManager.getConnection(conStr, getUser(), getPassword());
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
