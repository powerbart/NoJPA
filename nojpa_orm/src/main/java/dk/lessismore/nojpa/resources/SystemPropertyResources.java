package dk.lessismore.nojpa.resources;

import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by niakoi on 10/9/14.
 */
public class SystemPropertyResources extends Resources {
    private Properties properties;

    public SystemPropertyResources(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getResource(String key) {
        return properties.getProperty(key);
    }

    @Override
    public boolean gotResources() {
        return properties.isEmpty();
    }

    @Override
    public Enumeration getResourceNames() {
        return properties.propertyNames();
    }

    @Override
    public boolean canGetParameterNameList() {
        return false;
    }

    @Override
    public boolean setString(String key, String value) {
        return false;
    }
}
