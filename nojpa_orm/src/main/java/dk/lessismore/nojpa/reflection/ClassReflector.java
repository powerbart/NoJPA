package dk.lessismore.nojpa.reflection;

import java.util.*;

import dk.lessismore.nojpa.reflection.attributes.*;
import dk.lessismore.nojpa.reflection.db.model.ModelObject;

/**
 * This class is a factory which contains all of the reflected classes.
 * When ever you want an <tt>AttributeContainer</tt> of a class you must
 * call <tt>getAttributeContainer</tt>. If the class allready has been reflected
 * you will get an old copy of the Container from the cache. If not this class
 * will make the reflection; and place it in the cache; so that we do not make any unnessesary
 * work.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class ClassReflector {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClassReflector.class);

    /**
     * Cached reflected classes. key=classPath, value=AttributeContainer.
     */
    private static Map _reflectedClasses = new HashMap();

    public ClassReflector() {}

    /**
     * Gets an hashtable with the reflected classes as AttributeContainers
     */
    public static Map getReflectedClasses() {
        return _reflectedClasses;
    }

    /**
     * This will finde the AttributeContainer which the class of the attribute has.
     */
    public static AttributeContainer getAttributeContainer(Attribute attribute) {
        return getAttributeContainer(attribute.getAttributeClass());
    }

    public static AttributeContainer getAttributeContainer(Object object) {
        if(object instanceof ModelObject){
            return getAttributeContainer(((ModelObject) object).getInterface());
        } else {
            return getAttributeContainer(object.getClass());
        }
    }

    public static AttributeContainer getAttributeContainer(Class myClass) {
        synchronized(log){
            AttributeContainer container = (AttributeContainer) getReflectedClasses().get(myClass.getName());
            //log.debug("getAttributeContainer : 1 container = " + container);
            if(container == null) {
                //log.debug("getAttributeContainer : 2 ");
                //This class has not been reflected before. We reflect it and place it in the cache.
                container = new AttributeContainer();
                container.findAttributes(myClass, false, true, false);
                //log.debug("getAttributeContainer : 3 container = " + container);
                getReflectedClasses().put(myClass.getName(), container);
                //log.debug("getAttributeContainer : 4 ");
            }
            //log.debug("getAttributeContainer : 5 ");
            return container;
        }
    }
}
