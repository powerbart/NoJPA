package dk.lessismore.nojpa.spring.db;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.net.URL;
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
        return from(env, "spring");
    }

    public static Properties from(Environment env, String name) throws Exception {
        Properties props = new Properties();
        String driverName = env.getProperty(name + ".datasource.driverClassName");
        props.setProperty("driverName", driverName);

        props.setProperty("user", env.getProperty(name + ".datasource.username"));
        props.setProperty("password", env.getProperty(name + ".datasource.password"));
        props.setProperty("nrOfPoolConnections", env.getProperty(name + ".datasource.nrOfPoolConnections"));

        String url = env.getProperty(name + ".datasource.url");
        URI uri = new URI(new URI(url).getSchemeSpecificPart());
        props.setProperty("database", uri.getScheme());
        if (driverName.contains("h2")) {
            props.setProperty("databaseName", uri.getSchemeSpecificPart());
        } else {
            props.setProperty("ip", uri.getHost());
            props.setProperty("databaseName", url.substring(url.lastIndexOf('/') + 1));
            props.setProperty("port", Integer.toString(uri.getPort()));
        }
        return props;
    }
}
