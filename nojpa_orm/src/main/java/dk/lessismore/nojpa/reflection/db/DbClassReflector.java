package dk.lessismore.nojpa.reflection.db;

import java.lang.reflect.*;
import java.util.*;

import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.attributes.*;
import dk.lessismore.nojpa.reflection.*;
import dk.lessismore.nojpa.reflection.attributes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a factory which contains the different DbAttributeContainer.
 * You should call <tt>getDbAttributeContainer</tt> to get the container you are looking for.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE
 */
public class DbClassReflector {

    private static final Logger log = LoggerFactory.getLogger(DbClassReflector.class);

    /**
     * The DbAttributeContainers. (key=classname, value=DbAttributeContainer)
     */
    private static Map _reflectedClasses = new HashMap();

    public DbClassReflector() {}

    /**
     * The DbAttributeContainers. (key=classname, value=DbAttributeContainer)
     */
    public static Map getReflectedClasses() {
	    return _reflectedClasses;
    }
    public static DbAttributeContainer getDbAttributeContainer(DbAttribute dbAttribute) {
        return getDbAttributeContainer(dbAttribute.getAttribute().getAttributeClass());
    }
    public static DbAttributeContainer getDbAttributeContainer(Object object) {
        // TODO make this fix more general
        int dummy = 2+2;
        Class cls = object.getClass();
        if (Proxy.isProxyClass(object.getClass())) {
            cls =((ModelObject) object).getInterface();
        } else {
            cls = object.getClass();
        }
        return getDbAttributeContainer(cls);
    }

    /**
     * This methods look up the database attribute container in the hashtable. If the
     * container is not allready present; the method will try to attemp to create the container.
     * If its not possible; null will be returned.
     */
    public static synchronized DbAttributeContainer getDbAttributeContainer(Class myClass) {
//        synchronized(log){
            DbAttributeContainer container = (DbAttributeContainer)getReflectedClasses().get(myClass.getName());
            //log.debug("DbAttributeContainer:: getDbAttributeContainer:0");
            if(container == null) {
                container = new DbAttributeContainer();
                //log.debug("DbAttributeContainer:: getDbAttributeContainer. 1 ");
                AttributeContainer attributeContainer = ClassReflector.getAttributeContainer(myClass);
                if(container.setAttributeContainer(attributeContainer)){
                    //log.debug("DbAttributeContainer:: getDbAttributeContainer. 2 ");
                    getReflectedClasses().put(myClass.getName(), container);
                }
                else{
                    log.debug("DbAttributeContainer:: getDbAttributeContainer. XXX ");
                    container = null;
                }
            }
            //log.debug("DbAttributeContainer:: getDbAttributeContainer. 4 " + container);
            return container;
//        }
    }

    //public static Class<? extends ModelObjectInterface> getClass
}
