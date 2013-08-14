package dk.lessismore.reusable_v4.resources;
import java.util.*;
import java.net.*;
import java.io.*;

/**
 * This class represents the resources in a property file. The class can be
 * configured through the property file called <tt>resources.properties</tt>.
 * In this file you can configurate how often the resources should be loaded.
 * This is done through the variable <tt>reloadPeriod</tt>; which is measured in
 * minutes. If you do not want the resources to be reloaded; you
 * can specify this in the constructor. When it reloads the old resources
 * will be replaced with new ones from the property file. In this way you
 * can change the properties at runtime !
 * <br>NB: Remember that the property which this class should represent should be
 * in the classpath before execution of the program !
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class PropertyResources extends Resources {

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PropertyResources.class);
    /**
     * The property file.
     */
    private Properties _properties = null;

    /**
     * The file name of the property file.
     */
    private String _propertyFileName = "";

    /**
     * Indicates wether the property file should be reloaded at each hit.
     */
    private boolean _reloadOnEachHit = false;
    
    /**
     * Should we reload the properties.
     */
    private boolean _reload = false;

    /**
     * The reload period in miliseconds.
     */
    private long _reloadPeriod = 10000*60*1000; 

    /**
     * The time of the last reload.
     */
    private long _timeOfLastReload = 0;

    /**
     * The location of the configuration file of this class.
     */
    private String _resourceConfigurationFile = "/resources.properties";

    /**
     * @param reload If the resources should be reloaded.
     * @param propertyFileName The name of the property file. You do not
     * have to end the name with <tt>.properties</tt>.
     */
    public PropertyResources(String propertyFileName, boolean reload) {
        this(propertyFileName);
        _reload = reload;
    }

	public Map getMap(){
		
		Map propertyMap = new HashMap();
		Enumeration es = _properties.propertyNames();
		Object tmpObject = null;
		
		while(es.hasMoreElements()){
			tmpObject = es.nextElement();
			propertyMap.put(tmpObject, _properties.get(tmpObject));		
		}
		return propertyMap;		 
	}


    /**
     * @param propertyFileName The name of the property file. You do not
     * have to end the name with <tt>.properties</tt>.
     */
    public PropertyResources(String propertyFileName) {
        if(!propertyFileName.startsWith("/"))
            propertyFileName = "/"+propertyFileName;
        if(!propertyFileName.endsWith(".properties"))
            propertyFileName += ".properties";
        _propertyFileName = propertyFileName;
        reloadPropertyFile();
    }
    public void setReloadAtEachHit(boolean reloadOnEachHit) {
        _reloadOnEachHit = reloadOnEachHit;
    }
    /**
     * This method will reload the property file which the resources depend on. The reload
     * will only happen if reload is set to true. The reloadPeriod will allso
     * be reloaded as well so you can change this at runtime.
     */
    private synchronized void reloadPropertyFile() {
        Properties properties = loadPropertyFile(_propertyFileName);
        if(properties != null)
            _properties = properties;
        _timeOfLastReload = System.currentTimeMillis();
        if(isReloadable()) {
            Properties resourceConfiguration = loadPropertyFile(_resourceConfigurationFile);
            try {
                _reloadPeriod = Integer.parseInt(resourceConfiguration.getProperty("reloadPeriod"))*1000*60;
            } catch (Exception e) {
	        }
        }
    }

    /**
     * This method will try to load the property file. If this is not possible null
     * will be returned.
     */
    private Properties loadPropertyFile(String name) {
        System.out.println("Loading filename = " + name);
        URL propertyURL = Resources.class.getResource(name);
        if(propertyURL != null) {
            Properties properties = new Properties();
            try{
                log.debug("Loading : " + propertyURL);
                InputStream fi = propertyURL.openStream();
                properties.load(new InputStreamReader(fi, "UTF-8"));
		        fi.close();
                return properties;
            }catch(Exception e) {
		        log.error("some error when loadPropertyFile " + e);
	        }
        } else {
            System.out.println("ERROR: Cant file filename = '"+ name +"' ... ");
        }
        return null;
    }

    private boolean isReloadable() {
        return _reload;
    }
    /**
     * Have we passed the time when we should reload the propertyfile.
     */
    private boolean passedReloadTime() {
        long elapsedTime = (System.currentTimeMillis() - _timeOfLastReload);
        return elapsedTime >= _reloadPeriod;

    }

    /**
     * This method will check to see if we have passed the time to reload;
     * and if so reload the property file.
     */
    private synchronized void checkForReload() {
        if(_reloadOnEachHit)
            reloadPropertyFile();        
        else if(isReloadable()) {
            if(passedReloadTime())
                reloadPropertyFile();
        }
    }


    public String getResource(String key) {
        checkForReload();
        if(_properties != null)
            return _properties.getProperty(key);
        else
            return null;
    }
    public boolean gotResources() {
        return _properties != null;
    }
    public Enumeration getResourceNames() {
        checkForReload();
        if(_properties != null)
            return _properties.propertyNames();
        else
            return null;
    }
    public boolean canGetParameterNameList() {
        return true;
    }

    public synchronized boolean setString(String key, String value) {
        if(_properties != null) {
            try {
                _properties.setProperty(key, value);
                URL propertyURL = Resources.class.getResource(this._propertyFileName);
		        FileOutputStream fo = new FileOutputStream(propertyURL.getFile());
                _properties.store(fo, null);
		        fo.flush();
		        fo.close();
                return true;
            }catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

 public synchronized void remove(String key) {
        if(_properties != null) {
            try {
                _properties.remove(key);
                URL propertyURL = Resources.class.getResource(this._propertyFileName);
                if(propertyURL != null && propertyURL.getFile() != null && (new File(propertyURL.getFile())).exists()){
                    FileOutputStream fo = new FileOutputStream(propertyURL.getFile());
                    _properties.store(fo, null);
                    fo.flush();
                    fo.close();
                } else {
                    log.warn("Cant find property file for: " + this._propertyFileName, new Exception());
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
