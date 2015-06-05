package dk.lessismore.nojpa.cache;

import dk.lessismore.nojpa.reflection.db.model.ModelObject;

public class CacheListener {
    public ModelObject listener = null;
    public String fieldNameOnListener = null;

    public CacheListener(ModelObject listener, String fieldNameOnListener) {
        this.listener = listener;
        this.fieldNameOnListener = fieldNameOnListener;
    }
}

