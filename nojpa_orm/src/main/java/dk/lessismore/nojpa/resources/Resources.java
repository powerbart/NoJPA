package dk.lessismore.nojpa.resources;

import java.util.*;

/**
 * A resource is an attribute with a name and a string value. The resource can have different
 * origins. It can be from a property file; http request or an applet. This class is an
 * common interface to access resources that can come from different origins. By using this
 * interface your code will not be dependant on which kind of origin the resources come from,
 * which enables a better test fase.
 * <br>This class is declared abstract. So the specific code that works apon the different
 * resource origins, will be implementet in the sub class; and not in the super class.
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
abstract public class Resources {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Resources.class);

    public Resources() { }

    /**
     * This method will get the resource as a string. If the resource does not exists
     * null will be returned.
     */
    public String getString(String key){
        String resource = getResource(key);
        if(resource != null)
            return resource.trim();
        else
            return null;
    }

    /**
     * This method determines if the resource is an double.
     * If the resource does not exists or is not a double false will be returned.
     */
    public boolean isDouble(String key) {
        if(gotResource(key)) {
            try{
                new Double(getString(key));
                return true;
            }catch(NumberFormatException e) { }
        }
        return false;
    }

    /**
     * This method tries to converts the attribute to a double value.
     * @exception RuntimeException will be thrown if the conversion fails.
     */
    public double getDouble(String key) {
        if(gotResource(key)) {
            String value = getString(key);
            try{
                return (new Double(value)).doubleValue();
            }catch(NumberFormatException e) {
                throw new RuntimeException("Could not fetch 'double' resource value with key=" + key+" value="+value);
            }
        }
        else
            throw new RuntimeException("The resource " + key +" does not exists");
    }

    /**
     * The method determines if the resource can be converted to a long.
     */
    public boolean isLong(String key) {

        if(gotResource(key)) {
            try{
                new Long(getString(key));
                return true;
            }catch(NumberFormatException e) { }
        }
        return false;
    }
    /**
     * This method tries to converts the attribute to a long value.
     * @exception RuntimeException will be thrown if the conversion fails.
     */
    public long getLong(String key){
        if(gotResource(key)) {
            String value = getString(key);
            try{
                return (new Long(value)).longValue();
            }catch(NumberFormatException e) {
                throw new RuntimeException("Could not fetch 'long' resource value with key=" + key+" value="+value);
            }
        }
        else
            throw new RuntimeException("The resource " + key +" does not exists");
    }

    /**
     * The method determines if the resource can be converted to an int.
     */
    public boolean isInt(String key) {
        if(gotResource(key)) {
            try{
                new Integer(getString(key));
                return true;
            }catch(NumberFormatException e) { }
        }
        return false;
    }

    /**
     * This method tries to converts the attribute to an int value.
     * @exception RuntimeException will be thrown if the conversion fails.
     */
    public int getInt(String key){
        if(gotResource(key)) {
            String value = getString(key);
            try{
                return Integer.parseInt(value);
            }catch(NumberFormatException e) {
                throw new RuntimeException("Could not fetch 'int' resource value with key=" + key+" value="+value);
            }
        }
        else
            throw new RuntimeException("The resource " + key +" does not exists");
    }

    /**
     * The method determines if the resource can be converted to a boolean.
     */
    public boolean isBoolean(String key) {
        return gotResource(key);
    }

    /**
     * This method tries to converts the attribute to a boolean value. The conversion
     * can handle both <tt>true</tt>, <tt>on</tt> or <tt>1</tt>
     * @exception RuntimeException will be thrown if the conversion fails.
     */
    public boolean getBoolean(String key){
        if(gotResource(key)) {
            String value = getString(key);
            return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on") || value.equalsIgnoreCase("1");
        }
        else {
            String cause = "The resource " + key + " does not exists is missing or file";
            Exception exception = new Exception(cause);
            exception.printStackTrace();
            log.error("The resource " + key +" does not exists is missing or file", exception);
            return false;
        }
    }

    /**
     * The method determines if the resource can be converted to a class.
     */
    public boolean isClass(String key) {
        if(gotResource(key)) {
            String value = getString(key);
            try {
                Class.forName(value);
                return true;
            }catch(ClassNotFoundException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * This method tries to converts the attribute to a class.
     * @exception RuntimeException will be thrown if the conversion fails.
     */
    public Class getClass(String key) {
        if(gotResource(key)) {
            String value = getString(key);
            try {
                return Class.forName(value);
            }catch(ClassNotFoundException e) {
                throw new RuntimeException("Could not fetch 'Class' resource value with key=" + key+" value="+value);
            }
        }
        else
            throw new RuntimeException("The resource " + key +" does not exists");
    }

    /**
     * This method first tries to get the resource as a class; if that is possible;
     * a new instance of the class will be made. This is only possible if there is
     * an empty constructor.
     */
    public Object getInstanceOfClass(String key) {

        try{
            return getClass(key).newInstance();
        }catch(InstantiationException e) {
            throw new RuntimeException("Could not make object from key="+key+" msg="+e.getMessage());
        }catch(IllegalAccessException iae) {
            throw new RuntimeException("Could not make object from key="+key+" msg="+iae.getMessage());
        }
    }

    /**
     * This method assumes that the attribute contains a list where the
     * elements are separated with the given separator string. All the
     * elements will be read as a string and placed in a vector.
     * If the attribute is null; an empty list will be returned.
     */
    public List<String> getList(String key, String separator) {
        List<String> list = new ArrayList<String>(10);
        String listStr = getString(key);
        if(listStr != null) {
            StringTokenizer listTokens = new StringTokenizer(listStr, separator);
            while(listTokens.hasMoreTokens())
                list.add(listTokens.nextToken().trim());
        }
        return list;
    }

    /**
     * Gets a list from the attribute where the elements are separated with commas.
     */
    public List<String> getList(String key) {
        return getList(key, ",");
    }

    /**
     * Determines wether the resource by the given name is present in the
     * resources.
     */
    public boolean gotResource(String key) {
        return getString(key) != null;
    }

    /**
     * This method fetches a list of all accesable resource names, and identifies
     * the names which contains the given string.
     * The names which match (or partly match) will be returned in a vector list.
     */
    public List getMatchingResourceNames(String match) {
        List<String> matches = new ArrayList<String>(20);
        Enumeration es = getResourceNames();
        if(es != null) {
            while(es.hasMoreElements()) {
                String resourceName = (String)es.nextElement();
                if(resourceName.startsWith(match))
                    matches.add(resourceName);
            }
        }
        return matches;
    }

    /**
     * Gets a resource from the key/attributename. The resource will be returned
     * as a string. If the resource does not exists, null will be returned.
     */
    abstract public String getResource(String key) ;

    /**
     * This method validates the resource repository; and return <tt>true</tt>
     * if it has loaded the required resource file/repository; or <tt>false</tt>
     * if i could not load it. This could be that the file position of the
     * property file was wrong. Then this method may return false. Use this method
     * to see if all has been loaded well.
     */
    abstract public boolean gotResources() ;

    /**
     * This method returns a list of the keys/attributeNames in the resource.
     * It is not all kinds of resources which can give a full list of names; this
     * is the case for Applets. If so a null pointer will be returned. If you are not
     * sure if you can call this method; please call <tt>canGetParameterNameList</tt>;
     * to get an answer.
     */
    abstract public Enumeration getResourceNames();

    /**
     * This method returns wether its possible to get a list of the
     * resource names in the resources.
     */
    abstract public boolean canGetParameterNameList();

    /**
     * This method will erase the old value of a resource and substitute it with a new one.
     * This change will be persistant over time. (for a property file);
     */
    abstract public boolean setString(String key, String value);

}

