package dk.lessismore.reusable_v4.db.connectionpool;

import dk.lessismore.reusable_v4.pool.*;
import dk.lessismore.reusable_v4.resources.*;

/**
 * This class is a singelton containing a pool of connections to a given database. The database
 * properties is defined in the property file called "db". You can
 * specify the nr of pool connections in this file under the property
 * <tt>nrOfPoolConnections</tt>.
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class ConnectionPoolFactory  {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionPoolFactory.class);

    private static Resources resources = new PropertyResources("db");

    private static ResourcePool connectionPool = null;

    private static int nrOfPoolConnections = 20;

    public static int getNrOfPoolConnections() {
        if(resources.isInt("nrOfPoolConnections"))
            return resources.getInt("nrOfPoolConnections");
        else
            return nrOfPoolConnections;
    }

    public static ResourcePool getPool() {

        if(connectionPool == null) {
            log.debug("making pool");
            connectionPool = new ResourcePool(new ConnectionFactory(), getNrOfPoolConnections());
        }
        return connectionPool;
    }




}
