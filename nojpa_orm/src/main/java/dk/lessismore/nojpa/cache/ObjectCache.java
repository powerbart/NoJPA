package dk.lessismore.nojpa.cache;

import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectProxy;
import dk.lessismore.nojpa.reflection.util.ClassAnalyser;
import dk.lessismore.nojpa.resources.PropertyResources;
import dk.lessismore.nojpa.resources.Resources;
import dk.lessismore.nojpa.utils.MaxSizeMap;
import dk.lessismore.nojpa.utils.MaxSizeMaxTimeMap;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author LESS-IS-MORE
 * @version 1.0 21-5-2
 */
public class ObjectCache implements ObjectCacheInterface {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ObjectCache.class);

    //private Object mySync = new Object();

    private static Resources resources = new PropertyResources("ObjectCache");

    /**
     * The cached objects (key=primaryKey, value=object).
     */
    private final Map cachedObjects[] = new Map[]{new HashMap(), new HashMap()};

    /**
     * The array nr of the old bucket (should be 0 or 1).
     */
    private int nrOfOldBucket = 0;

    /**
     * The class type of the objects in the cache.
     */
    private Class targetClass = null;

    /**
     * The name of the class.
     */
    private String className = null;

    /**
     * The size of the cache in each bucket.
     */
    private int maxCacheSize = 2000;
    private boolean hasLoadedMaxSize = false;

    /**
     * Cache disabled or enabled. For test use.
     */
    private boolean cacheOn = true;
    private boolean hasLoadedCacheOn = false;

    private long numberOfFoundInCache = 0;
    private long numberOfNotFoundInCache = 0;

    public ObjectCache(Class targetClass) {
        this.targetClass = targetClass;
    }

    public long getNumberOfFoundInCache() {
        return numberOfFoundInCache;
    }

    public long getNumberOfNotFoundInCache() {
        return numberOfNotFoundInCache;
    }

    public String getClassName() {
        if (className == null) {
            className = ClassAnalyser.getClassName(targetClass);
        }
        return className;
    }

    public int getMaxCacheSize() {
        if (!hasLoadedMaxSize) {
            if (resources.isInt("objectcache." + getClassName() + ".maxCacheSize")) {
                maxCacheSize = resources.getInt("objectcache." + getClassName() + ".maxCacheSize");
            } else if (resources.isInt("objectcache.maxCacheSize")) {
                maxCacheSize = resources.getInt("objectcache.maxCacheSize");
            }
            hasLoadedMaxSize = true;
        }

        return maxCacheSize;
    }

    public boolean isCacheOn() {
        if (!hasLoadedCacheOn) {
            if (resources.isBoolean("objectcache." + getClassName() + ".cacheOn")) {
                cacheOn = resources.getBoolean("objectcache." + getClassName() + ".cacheOn");
            } else if (resources.isBoolean("objectcache.cacheOn")) {
                cacheOn = resources.getBoolean("objectcache.cacheOn");
            }
            hasLoadedCacheOn = true;
        }
        return cacheOn;
    }

    public void setCacheOn(boolean cacheOn) {
        this.cacheOn = cacheOn;
    }

    public synchronized int getNrOfObjectsInCache() {
        //synchronized (cachedObjects) {
        return getNewBucket().size() + getOldBucket().size();
        //}
    }

    public Map[] getCachedObjects() {
        return cachedObjects;
    }

    public Map getNewBucket() {
        return getCachedObjects()[((nrOfOldBucket + 1) % 2)];
    }

    public Map getOldBucket() {
        return getCachedObjects()[(nrOfOldBucket % 2)];
    }

    public boolean isFull(Map bucket) {
        return bucket.size() == getMaxCacheSize();
    }


    /**
     * This method will put the object in the cache.
     */
    public synchronized void putInCache(String key, Object object) {
        ////log.debug("putInCache:START "+key+" / "+targetClass.getName());
//    log.error("putInCache: 1 "+key + " " + object + " / " + object.getClass(), new Exception("DEBUG"));
        //synchronized (cachedObjects) {
        try {
            ////log.debug("putInCache: 2 "+ key);
            if (!isCacheOn()) {
                ////log.debug("putInCache: 3 "+key);
                ////log.debug("putInCache: END "+key);
                return;
            }
            if (isFull(getNewBucket())) {
                ////log.debug("putInCache: 4 "+key);
                ////log.debug("putInCache: shiftBuckets() "+key);
                shiftBuckets();
            }

            Map newBucket = getNewBucket();
            Map oldBucket = getOldBucket();
            if (newBucket.containsKey(key)) {
                ////log.debug("putInCache: newBucket.containsKey(key) = true "+key);
                ////log.debug("putInCache: END "+key);
                return;
            }
            if (oldBucket.containsKey(key)) {
                ////log.debug("putInCache: oldBucket.containsKey(key) = true "+key);
                Object o = oldBucket.get(key);
                oldBucket.remove(o);
                newBucket.put(key, o);
            } else {
                ////log.debug("putInCache: oldBucket.containsKey(key) = false "+key);
                if (object instanceof CacheEntry) {
                    ////log.debug("putInCache: object instanceof CacheEntry "+key);
                    newBucket.put(key, object);
                } else {
                    ////log.debug("putInCache: NOT object instanceof CacheEntry "+key);
                    CacheEntry entry = new CacheEntry((ModelObject) object);
                    newBucket.put(key, entry);
                }
            }
        } catch (Exception e) {
            log.error("putInCache:: some error ... " + e);
            e.printStackTrace();
        }
        ////log.debug("putInCache: END");
        //}
    }

    public synchronized void removeListener(String primaryKey, ModelObject listener, String fieldNameOnListener) {
        ////log.debug("removeListener::START " + primaryKey);
        ////log.debug("removeListener::1 ");
        //synchronized (cachedObjects) {
        ////log.debug("removeListener::2 " + primaryKey);
        try {
            ////log.debug("removeListener::3 "  + primaryKey);
            CacheEntry entry = (CacheEntry) getNewBucket().get(primaryKey);
            ////log.debug("removeListener::4 " + primaryKey);
            if (entry != null) {
                ////log.debug("removeListener::5 " + primaryKey);
                int counter = 0;
                for (int i = 0; i < entry.listOfListeners.size(); i++) {
                    ////log.debug("removeListener::6 " + primaryKey);
                    CacheListener cacheListener = (CacheListener) entry.listOfListeners.get(i);
                    ////log.debug("removeListener::7 " + cacheListener.listener);
                    if (cacheListener.listener.equals(listener)) {
                        ////log.debug("removeListener::8 " + primaryKey);
                        entry.listOfListeners.remove(i);
                        i--;
                        ////log.debug("removeListener::9 " + primaryKey);
                    }
                    ////log.debug("removeListener::10 " + primaryKey);
                }
                ////log.debug("removeListener::11 " + primaryKey);
            } else {
                ////log.debug("removeListener::12 " + primaryKey);
                entry = (CacheEntry) getOldBucket().get(primaryKey);
                ////log.debug("removeListener::13 " + primaryKey);
                if (entry != null) {
                    ////log.debug("removeListener::14 " + primaryKey);
                    int counter = 0;
                    for (int i = 0; i < entry.listOfListeners.size(); i++) {
                        ////log.debug("removeListener::15 " + primaryKey);
                        CacheListener cacheListener = (CacheListener) entry.listOfListeners.get(i);
                        ////log.debug("removeListener::16 " + cacheListener.listener);
                        if (cacheListener.listener.equals(listener)) {
                            ////log.debug("removeListener::17 " + primaryKey);
                            entry.listOfListeners.remove(i);
                            ////log.debug("removeListener::18 " + primaryKey);
                        }
                        ////log.debug("removeListener::19 " + primaryKey);
                    }
                    ////log.debug("removeListener::20 " + primaryKey);
                }
                ////log.debug("removeListener::21 " + primaryKey);
            }
            ////log.debug("removeListener::22 " + primaryKey);
        } catch (Exception e) {
            log.error("removeListener:: some error ... " + e);
            e.printStackTrace();
        }
        //}
        ////log.debug("removeListener::23 " + primaryKey);
        ////log.debug("removeListener::END " + primaryKey);
    }


    public synchronized boolean addListener(String primaryKey, ModelObject listener, String fieldNameOnListener) {
        //log.debug(":addListener:START " + primaryKey);
        //synchronized (cachedObjects) {
        try {
            CacheEntry entry = (CacheEntry) getNewBucket().get(primaryKey);
            if (entry != null) {
                entry.listOfListeners.add(new CacheListener(listener, fieldNameOnListener));
                ////log.debug("addListener::END1 " + primaryKey);
                return true;
            } else {
                entry = (CacheEntry) getOldBucket().get(primaryKey);
                if (entry != null) {
                    entry.listOfListeners.add(new CacheListener(listener, fieldNameOnListener));
                    ////log.debug("addListener::END2 " + primaryKey);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("addListener:: some error ... " + e);
            e.printStackTrace();
        }
        //}
        ////log.debug("addListener::END3");
        return false;
    }


    /**
     * This method will remove the object from the cache with the given primaryKey.
     */
    public synchronized void removeFromCache(String primaryKey) {
        //log.debug("removeFromCache::START for "+primaryKey+" / "+targetClass.getName());
        try {
            ////log.debug("removeFromCache::3 " + primaryKey);
            Map bucket = getNewBucket();
            CacheEntry entry = (CacheEntry) bucket.get(primaryKey);
            if(entry == null){
                bucket = getOldBucket();
                entry = (CacheEntry) bucket.get(primaryKey);
            }

            if(entry != null){
                for (int i = 0; i < entry.listOfListeners.size(); i++) {
                    //We need to remove a list of the Ref's ... since the same address can be use and the some object many times or or many diff objects
                    CacheListener cacheListener = (CacheListener) entry.listOfListeners.get(i);
                    cacheListener.listener.removeAllListnersForFieldName(cacheListener.fieldNameOnListener);
                }
                bucket.remove(primaryKey);
            }
        } catch (Exception e) {
            log.error("removeFromCache:: some error ... " + e);
            e.printStackTrace();
        }
    }

    /**
     * This method gets an object with the given primary key from the cache.
     */
    public synchronized ModelObject getFromCache(String primaryKey) {
//    log.debug("getFromCache::START " + primaryKey + " sizes(n:"+ getNewBucket().size() +"-o:"+ getOldBucket().size() +")");
        //synchronized (cachedObjects) {
        try {
            Map newBucket = getNewBucket();
            CacheEntry entry = (CacheEntry) newBucket.get(primaryKey);
            if (entry == null) {
                Map oldBucket = getOldBucket();
                entry = (CacheEntry) oldBucket.get(primaryKey);
                if (entry != null) {
                    oldBucket.remove(primaryKey);
                    putInCache(primaryKey, entry);
                    ////log.debug("getFromCache::END1 " + primaryKey);
                    numberOfFoundInCache++;
                    ModelObject toReturn = entry.objectToCache;
                    if (toReturn.getClass().getName().indexOf("$") == -1) {
                        toReturn = (ModelObject) Proxy.newProxyInstance(
                                this.getClass().getClassLoader(),
                                new Class<?>[]{targetClass, ModelObject.class},
                                (ModelObjectProxy) toReturn);
                    }
                    return toReturn;
                } else {
                    ////log.debug("getFromCache::END2 " + primaryKey);
                    //numberOfNotFoundInCache++;
                    return null;
                }
            } else {
                ////log.debug("getFromCache::END3 " + primaryKey);
                numberOfFoundInCache++;
//          log.debug("getFromCache::END3 " + primaryKey + " returns " + entry.objectToCache + " / " + entry.objectToCache.getClass() );
                ModelObject toReturn = entry.objectToCache;
                if (toReturn.getClass().getName().indexOf("$") == -1) {
                    toReturn = (ModelObject) Proxy.newProxyInstance(
                            this.getClass().getClassLoader(),
                            new Class<?>[]{targetClass, ModelObject.class},
                            (ModelObjectProxy) toReturn);
                }
                return toReturn;
            }
        } catch (Exception e) {
            log.error("getFromCache:: some error ... " + e);
            e.printStackTrace();
            ////log.debug("getFromCache::END4 "  + primaryKey);
            return null;
        }
        //}
    }

    private synchronized void shiftBuckets() {
        //log.debug("shiftBuckets::START");
        //synchronized (cachedObjects) {
        try {
            Map oldBucket = getOldBucket();
            int i = 0;
            while (!oldBucket.isEmpty()) {
                ////log.debug("shiftBuckets::loop "+i+"/"+oldBucket.size());
                Iterator ite = oldBucket.values().iterator();
                CacheEntry entry = (CacheEntry) ite.next();
                ////log.debug("shiftBuckets::loop removing " + entry.objectToCache);
                removeFromCache("" + entry.objectToCache);
            }
            oldBucket.clear();
            nrOfOldBucket++;
            nrOfOldBucket %= 2;
        } catch (Exception e) {
            log.error("shiftBuckets:: some error ... " + e);
            e.printStackTrace();
        }
        //}
        ////log.debug("shiftBuckets::END");
    }

    public synchronized void clear() {
        //log.debug("clean::START");
        //synchronized (cachedObjects) {
        try {
            shiftBuckets();
            shiftBuckets();
        } catch (Exception e) {
            log.error("clear:: some error ... " + e);
            e.printStackTrace();
        }
        //}
        ////log.debug("clean::END");
    }


    MaxSizeMaxTimeMap<String> uniqueRelation = null;

    private MaxSizeMaxTimeMap<String> getUniqueRelationMap(){
        if(uniqueRelation == null){
            uniqueRelation = new MaxSizeMaxTimeMap<String>(getMaxCacheSize(), 10 * 60);
        }
        return uniqueRelation;
    }

    @Override
    public String getUniqueRelation(String key) {
        return getUniqueRelationMap().get(key);
    }

    @Override
    public void putUniqueRelation(String key, String objectID) {
        synchronized (this) {
            getUniqueRelationMap().put(key, objectID);
        }
    }

//    @Override
//    public void clearUniqueRelation() {
//        synchronized (this) {
//            getUniqueRelationMap().clear();
//        }
//    }


//    MaxSizeMap<String, String> refRelation = null;
//
//    private MaxSizeMap<String, String> getRefRelationMap(){
//        if(refRelation == null){
//            refRelation = new MaxSizeMap<String, String>(getMaxCacheSize());
//        }
//        return refRelation;
//    }
//
//
//    @Override
//    public String getRefRelation(String refKey) {
//        return getRefRelationMap().get(refKey);
//    }
//
//    //remoteObjectID, key,
//    @Override
//    public void putRemoveRefListener(String remoteObjectID, String key, Class<? extends ModelObjectInterface> selectClass, String objectID) {
//        ModelObject fromCache = getFromCache(key);    TODO
//    }
//
//    @Override
//    public void putRefRelation(String refKey, String objectID) {
//        synchronized (this) {
//            getRefRelationMap().put(refKey, objectID);
//        }
//    }


}
