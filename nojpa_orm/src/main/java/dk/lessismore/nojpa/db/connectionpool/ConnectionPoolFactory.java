package dk.lessismore.nojpa.db.connectionpool;

import dk.lessismore.nojpa.pool.ResourcePool;
import dk.lessismore.nojpa.resources.PropertyResources;
import dk.lessismore.nojpa.resources.Resources;
import dk.lessismore.nojpa.resources.SystemPropertyResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Properties;

/**
 * This class is a singelton containing a pool of connections to a given database. The database
 * properties is defined in the property file called "db". You can
 * specify the nr of pool connections in this file under the property
 * <tt>nrOfPoolConnections</tt>...
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public class ConnectionPoolFactory  {

    private static final Logger log = LoggerFactory.getLogger(ConnectionPoolFactory.class);

    private static Resources resources;
    private static ResourcePool connectionPool = null;
    private static HashMap<String, ResourcePool> connectionPoolsWithNames = new HashMap<String, ResourcePool>(3);

    public static void configure(Properties prop) {
        resources = new SystemPropertyResources(prop);
    }
    public static synchronized ResourcePool getPool() {

        if (connectionPool == null) {
            log.debug("making new pool, maybe from resources("+ resources +") ....  ");
            if (resources == null) {
                resources = new PropertyResources("db");
            }
            int poolSize = resources.isInt("nrOfPoolConnections") ? resources.getInt("nrOfPoolConnections") : 20;
            connectionPool = new ResourcePool(new ConnectionFactory(resources), poolSize);
        }
        return connectionPool;
    }


    public static synchronized ResourcePool getPool(String poolName) {
        ResourcePool connectionPool = connectionPoolsWithNames.get(poolName);
        if (connectionPool == null) {
            log.debug("making pool");
            if (resources == null) {
                resources = new PropertyResources("db");
            }
            int poolSize = resources.isInt(poolName + ".nrOfPoolConnections") ? resources.getInt(poolName + ".nrOfPoolConnections") : 20;
            connectionPool = new ResourcePool(new ConnectionFactory(resources, poolName), poolSize);
            connectionPoolsWithNames.put(poolName, connectionPool);
        }
        return connectionPool;
    }

    public static synchronized boolean havingPool() {
        return connectionPool != null;
    }

}
