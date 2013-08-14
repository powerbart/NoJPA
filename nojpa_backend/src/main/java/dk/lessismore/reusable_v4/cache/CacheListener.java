package dk.lessismore.reusable_v4.cache;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObject;

public class CacheListener {
    public ModelObject listener = null;
    public String fieldNameOnListener = null;
    public CacheListener(ModelObject listener, String fieldNameOnListener){
	this.listener = listener;
	this.fieldNameOnListener = fieldNameOnListener;
    }
}

