package dk.lessismore.nojpa.cache;

import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import java.util.*;

/**
 * This class is a factory that contains the different caches for the model objects; which is persisted in the db reflection library.
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 21-5-2
 */
public class ObjectCacheFactory {
  private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ObjectCacheFactory.class);
  private static Object lock = new Object();

  /** Singelton. */
  private static final ObjectCacheFactory objectCacheFactory = new ObjectCacheFactory();

  /** The different object caches. (key=class,value=DbObjectCache) */
  private final Map objectsCache = new HashMap();
  private final Map objectArraysCache = new HashMap();

  private ObjectCacheFactory() {

  }

  /** The different object caches. (key=class,value=DbObjectCache) */
  public Map getObjectsCache() {
    return objectsCache;
  }

  public Map getObjectArraysCache() {
    return objectArraysCache;
  }

  /** Call this to get an instance of the singelton. */
  public static ObjectCacheFactory getInstance() {
    return objectCacheFactory;
  }

  /** Flushes the cache. */
  public void clear() {
    synchronized (objectsCache) {
      objectsCache.clear();
    }
    synchronized (objectArraysCache) {
      objectArraysCache.clear();
    }

  }

  public ObjectCache getObjectCache(Object object) {
    //log.debug("getObjectCache-BEFORE");
    synchronized (objectsCache) {
      //log.debug("getObjectCache-AFTER");
        if(object instanceof ModelObject){
            Class<? extends ModelObjectInterface> aClass = ((ModelObject) object).getInterface();
            return getObjectCache(aClass);
        } else {
            return getObjectCache(object.getClass());
        }
    }
  }

  public ObjectArrayCache getObjectArrayCache(Object object) {
    synchronized (objectArraysCache) {
      return getObjectArrayCache(object.getClass());
    }
  }

  /** Gets an copy of the desired object cache. If the cache does not exists a new cache will be made for the class type. */
  public ObjectCache getObjectCache(Class targetClass) {
    if(targetClass == null){
      throw new RuntimeException("Call with null");
    }
    synchronized (objectsCache) {
      ObjectCache objectCache = (ObjectCache) getObjectsCache().get(targetClass);
      if (objectCache != null) {
        return objectCache;
      } else {
        objectCache = new ObjectCache(targetClass);
        getObjectsCache().put(targetClass, objectCache);
      }
      return objectCache;
    }
  }

  public ObjectArrayCache getObjectArrayCache(Class targetClass) {
    synchronized (objectArraysCache) {
      ObjectArrayCache objectArrayCache = (ObjectArrayCache) getObjectArraysCache().get(targetClass);
      if (objectArrayCache != null) {
        return objectArrayCache;
      } else {
        objectArrayCache = new ObjectArrayCache(targetClass);
        getObjectArraysCache().put(targetClass, objectArrayCache);
      }
      return objectArrayCache;
    }
  }

}
