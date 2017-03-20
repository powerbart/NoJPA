package dk.lessismore.nojpa.cache;

import dk.lessismore.nojpa.reflection.util.ClassAnalyser;
import dk.lessismore.nojpa.resources.PropertyResources;
import dk.lessismore.nojpa.resources.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is a cache which can contain objectArrays of a specific class type. The cache algoritmen is like this: <ul> <li>We have to hashTable buckets.
 * <li>First we fill up the first bucket. When it is filled; all new cached objectArrays will be placed in the new bucket. <li>When a request for an objectArray
 * comes; the cache looks in the old bucket for it. if it exists there; the cache moves the objectArray to the new bucket. If not; the cache looks in the new
 * bucket. If its there it returns a copy of it. If not a null pointer will be returned. <ul>If the new bucket is filled; before all of the objectArrays from
 * the old bucket have been moved; the old bucket will be cleared, and now this will be the new bucket. </ul> In this way; we do not throw away valuable
 * objectArray which is offen requested.
 *
 * @author LESS-IS-MORE
 * @version 1.0 21-5-2
 */
public class ObjectArrayCache implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(ObjectArrayCache.class);


    private static Resources resources = new PropertyResources("ObjectArrayCache");
    //private Object sync = new Object();

    /**
     * The cached objectArrays (key=primaryKey, value=objectArray).
     */
    private final Map cachedObjectArrays[] = new Map[]{new HashMap(), new HashMap()};


    /**
     * The array nr of the old bucket (should be 0 or 1).
     */
    private int nrOfOldBucket = 0;

    /**
     * The class type of the objectArrays in the cache.
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

    /**
     * Cache disabled or enabled. For test use.
     */
    private boolean cacheOn = true;

    public ObjectArrayCache(Class targetClass) {
        this.targetClass = targetClass;
        try {
            if (resources.isInt("objectArraycache." + getClassName() + ".maxCacheSize")) {
                maxCacheSize = resources.getInt("objectArraycache." + getClassName() + ".maxCacheSize");
            } else if (resources.isInt("objectArraycache.maxCacheSize")) {
                maxCacheSize = resources.getInt("objectArraycache.maxCacheSize");
            } else {
                log.debug("Cant find resources for ObjectArrayCache.properties - will use standard values: maxCacheSize(" + maxCacheSize + ")");
            }
        } catch (Exception e) {
        }
        try {
            if (resources.isBoolean("objectArraycache." + getClassName() + ".cacheOn")) {
                cacheOn = resources.getBoolean("objectArraycache." + getClassName() + ".cacheOn");
            } else if (resources.isBoolean("objectArraycache.cacheOn")) {
                cacheOn = resources.getBoolean("objectArraycache.cacheOn");
            } else {
                log.debug("Cant find resources for ObjectArrayCache.properties - will use standard values: cacheOn(" + cacheOn + ")");
            }
        } catch (Exception e) {
        }
    }

    public String getClassName() {
        if (className == null) {
            className = ClassAnalyser.getClassName(targetClass);
        }
        return className;
    }

    public int getMaxCacheSize() {

        return maxCacheSize;
    }

    public boolean isCacheOn() {
        return cacheOn;
    }

    public void setCacheOn(boolean cacheOn) {
        this.cacheOn = cacheOn;
    }

    public int getNrOfObjectArraysInCache() {
        synchronized (cachedObjectArrays) {
            return getNewBucket().size() + getOldBucket().size();
        }
    }

    public Map[] getCachedObjectArrays() {
        return cachedObjectArrays;
    }

    public Map getNewBucket() {
        return getCachedObjectArrays()[((nrOfOldBucket + 1) % 2)];
    }

    public Map getOldBucket() {
        return getCachedObjectArrays()[(nrOfOldBucket % 2)];
    }

    public boolean isFull(Map bucket) {
        return bucket.size() == getMaxCacheSize();
    }

    private void putObjectArrayInCache(Map bucket, Object object, String key) {
        bucket.put(key, object);
    }

    /**
     * This method will put the objectArray in the cache.
     */
    public synchronized void putInCache(String key, Object objectArray) {
        //synchronized (cachedObjectArrays) {
        if (!isCacheOn()) {
            return;
        }
        //We need to move an maybe old copi from oldBucket to NewBucket
        Object cache = getFromCache(key);

        Map newBucket = getNewBucket();
        if (!isFull(newBucket)) {
            putObjectArrayInCache(newBucket, objectArray, key);
        } else {
            shiftBuckets();
            putObjectArrayInCache(getNewBucket(), objectArray, key);
        }
        //}
    }

    /**
     * This method will remove the objectArray from the cache with the given primaryKey.
     */
    public synchronized void removeFromCache(String primaryKey) {
        //synchronized (cachedObjectArrays) {
        Object objectArray = getNewBucket().remove(primaryKey);
        if (objectArray == null) {
            getOldBucket().remove(primaryKey);
        }
        //}
    }

    /**
     * This method gets an objectArray with the given primary key from the cache.
     */
    public synchronized Object getFromCache(String primaryKey) {
        //synchronized (cachedObjectArrays) {
        Map newBucket = getNewBucket();
        Object objectArray = newBucket.get(primaryKey);
        if (objectArray == null) {
            Map oldBucket = getOldBucket();
            objectArray = oldBucket.get(primaryKey);
            if (objectArray != null) {
                oldBucket.remove(primaryKey);
                putInCache(primaryKey, objectArray);
                return objectArray;
            } else {
                return null;
            }
        } else {
            return objectArray;
        }
        //}
    }

    private synchronized void shiftBuckets() {
        Map oldBucket = getOldBucket();
        oldBucket.clear();
        nrOfOldBucket++;
        nrOfOldBucket %= 2;
    }

    public synchronized void clear() {
        //synchronized (cachedObjectArrays) {
        for (int i = 0; i < this.cachedObjectArrays.length; i++) {
            cachedObjectArrays[i].clear();
        }
        //}
    }
}
