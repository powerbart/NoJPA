package dk.lessismore.nojpa.db;

import org.springframework.core.env.Environment;

import java.net.URI;
import java.util.Properties;

/**
 * Created by seb on 10/9/14.
 */
public class NoJpaDatabaseProperties {

    /*
    driverName=com.mysql.jdbc.Driver
ip=localhost
port=3306
databaseName=reqxldb?characterEncoding=UTF-8
database=mysql
nrOfPoolConnections=10
# TODO change to dbuser & dbpasswd (user is clashing with other system.properties)
user=root
password=1234

spring.datasource.driverClassName=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/reqxldb
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.nrOfPoolConnections=10
     */
    public static Properties from(Environment env) throws Exception {
        Properties props = new Properties();
        props.setProperty("driverName", env.getProperty("spring.datasource.driverClassName"));

        props.setProperty("user", env.getProperty("spring.datasource.username"));
        props.setProperty("password", env.getProperty("spring.datasource.password"));
        props.setProperty("nrOfPoolConnections", env.getProperty("spring.datasource.nrOfPoolConnections"));

        String url = env.getProperty("spring.datasource.url");
        URI uri = new URI(new URI(url).getSchemeSpecificPart());
        props.setProperty("ip", uri.getHost());
        props.setProperty("database", uri.getScheme());
        props.setProperty("databaseName", url.substring(url.lastIndexOf('/') + 1));
        props.setProperty("port", Integer.toString(uri.getPort()));

        return props;
    }
}
