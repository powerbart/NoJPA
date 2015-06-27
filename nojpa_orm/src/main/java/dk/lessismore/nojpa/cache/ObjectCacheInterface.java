package dk.lessismore.nojpa.cache;

import dk.lessismore.nojpa.reflection.db.model.ModelObject;

import java.io.Serializable;
import java.util.Map;

/**
 * Created : by IntelliJ IDEA.
 * User: jos
 * Date: Oct 1, 2008
 * Time: 2:28:35 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ObjectCacheInterface extends Serializable {
    long getNumberOfFoundInCache();

    long getNumberOfNotFoundInCache();

    String getClassName();

    int getMaxCacheSize();

    boolean isCacheOn();

    void setCacheOn(boolean cacheOn);

    int getNrOfObjectsInCache();

    Map[] getCachedObjects();

    Map getNewBucket();

    Map getOldBucket();

    boolean isFull(Map bucket);

    void putInCache(String key, Object object);

    void removeListener(String primaryKey, ModelObject listener, String fieldNameOnListener);

    boolean addListener(String primaryKey, ModelObject listener, String fieldNameOnListener);

    void removeFromCache(String primaryKey);

    Object getFromCache(String primaryKey);

    void clear();

    String getUniqueRelation(String key);

    void putUniqueRelation(String key, String objectID);
}
