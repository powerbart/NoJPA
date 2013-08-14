package dk.lessismore.reusable_v4.cache;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObject;
import java.util.*;

public class CacheEntry {
    public ModelObject objectToCache = null;
    public List listOfListeners = new ArrayList();
    public CacheEntry(ModelObject objectToCache){ this.objectToCache = objectToCache; }
}
