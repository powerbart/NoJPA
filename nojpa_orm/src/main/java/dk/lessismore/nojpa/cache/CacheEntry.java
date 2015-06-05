package dk.lessismore.nojpa.cache;

import dk.lessismore.nojpa.reflection.db.model.ModelObject;

import java.util.ArrayList;
import java.util.List;

public class CacheEntry {
    public ModelObject objectToCache = null;
    public List listOfListeners = new ArrayList();

    public CacheEntry(ModelObject objectToCache) {
        this.objectToCache = objectToCache;
    }
}
