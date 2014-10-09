package dk.lessismore.nojpa.spring;

import junit.framework.Assert;
import org.junit.Test;
import dk.lessismore.nojpa.spring.db.NoJpaDatabaseProperties;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.net.URI;
import java.net.URL;

/**
 * Created by niakoi on 10/9/14.
 */
public class Properties {

    /*
    spring.datasource.driverClassName=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/reqxldb
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.nrOfPoolConnections=10
     */
    @Test
    public void test_01() throws Exception {
        StandardEnvironment env = new StandardEnvironment();

        java.util.Properties props = new java.util.Properties();
        props.setProperty("spring.datasource.driverClassName", "com.mysql.jdbc.Driver");
        props.setProperty("spring.datasource.url", "jdbc:mysql://localhost:3306/reqxldb?useUnicode=true");
        props.setProperty("spring.datasource.username", "root");
        props.setProperty("spring.datasource.password", "1234");
        props.setProperty("spring.datasource.nrOfPoolConnections", "10");

        env.getPropertySources().addFirst(new PropertiesPropertySource("test", props));
        java.util.Properties from = NoJpaDatabaseProperties.from(env);

        Assert.assertEquals(from.getProperty("driverName"), "com.mysql.jdbc.Driver");
        Assert.assertEquals(from.getProperty("user"), "root");
        Assert.assertEquals(from.getProperty("password"), "1234");
        Assert.assertEquals(from.getProperty("nrOfPoolConnections"), "10");
        Assert.assertEquals(from.getProperty("ip"), "localhost");
        Assert.assertEquals(from.getProperty("database"), "mysql");
        Assert.assertEquals(from.getProperty("databaseName"), "reqxldb?useUnicode=true");
        Assert.assertEquals(from.getProperty("port"), "3306");
    }
}
