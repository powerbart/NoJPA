package dk.lessismore.nojpa.reflection.db.model;

import dk.lessismore.nojpa.cache.ObjectArrayCache;
import dk.lessismore.nojpa.cache.ObjectCache;
import dk.lessismore.nojpa.cache.ObjectCacheFactory;
import dk.lessismore.nojpa.guid.GuidFactory;
import dk.lessismore.nojpa.properties.Default;
import dk.lessismore.nojpa.properties.PropertiesProxy;
import dk.lessismore.nojpa.reflection.db.AssociationConstrain;
import dk.lessismore.nojpa.reflection.db.DbClassReflector;
import dk.lessismore.nojpa.reflection.db.DbObjectDeleter;
import dk.lessismore.nojpa.reflection.db.DbObjectReader;
import dk.lessismore.nojpa.reflection.db.DbObjectWriter;
import dk.lessismore.nojpa.reflection.db.annotations.DbStrip;
import dk.lessismore.nojpa.reflection.db.annotations.DoRemoteCache;
import dk.lessismore.nojpa.reflection.db.annotations.ModelObjectMethodListener;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttribute;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttributeContainer;
import dk.lessismore.nojpa.reflection.util.ClassAnalyser;
import dk.lessismore.nojpa.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Id;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * This dynamically implements the getters and setters as well as various other ModelObject methods.
 */
public class ModelObjectProxy implements ModelObject, InvocationHandler {

    private final Class<? extends ModelObjectInterface> interfaceClass;
    private boolean doRemoteCache = true;
    private Object proxyObject;
    private String objectID = GuidFactory.getInstance().makeGuid();
    private final static HashMap<String, String> methodListenerMap = new HashMap<String, String>();


    public static <T extends ModelObjectInterface> T create(Class<T> interfaceClass) {
        ModelObjectProxy object = new ModelObjectProxy(interfaceClass);
        object.proxyObject = Proxy.newProxyInstance(
	        interfaceClass.getClassLoader(),
	        new Class<?>[] { interfaceClass, ModelObject.class, },
            object);
        T toReturn = (T) object.proxyObject;
//     log.info("Making new object of type " + interfaceClass + " with objectID("+ toReturn +") ", new Exception("THIS_IS_NOT_AN_EXCEPTION__JUST_DEBUG"));

        return toReturn;
    }


    public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
        String name = method.getName();
        if(method.getDeclaringClass().isAssignableFrom(ModelObjectInterface.class)
                || ModelObject.class.isAssignableFrom(method.getDeclaringClass())) {
            return method.invoke(this, objects);
        }

        if(name.startsWith("get") && (objects == null || objects.length == 0)) {
            ModelObjectMethodListener annotation = method.getAnnotation(ModelObjectMethodListener.class);
            ModelObjectMethodListener.MethodListener methodListener = null;
            if(annotation != null && annotation.methodListener() != null){
                if(!methodListenerMap.containsKey(Thread.currentThread().getId() +":"+ method.getName())){
                    methodListenerMap.put(Thread.currentThread().getId() +":"+ method.getName(), "1");
                    try{
                        methodListener = annotation.methodListener().newInstance();
                        methodListener.preRun(object, method.getName(), objects);
                    } catch (Exception e){
                        log.error("Error when calling preRun " + annotation.methodListener().getCanonicalName() + " on " + interfaceClass.getCanonicalName() + ":" + method.getName() + " .... : " + e, e);
                    }
                    methodListenerMap.remove(Thread.currentThread().getId() +":"+ method.getName());
                }
            }
            Object association = getAssociation(method);

            if(methodListener != null){
                if(!methodListenerMap.containsKey(Thread.currentThread().getId() +":"+ method.getName())){
                    methodListenerMap.put(Thread.currentThread().getId() +":"+ method.getName(), "1");
                    try{
                        methodListener = annotation.methodListener().newInstance();
                        association = methodListener.postRun(object, method.getName(), association, objects);
                    } catch (Exception e){
                        log.error("Error when calling postRun " + annotation.methodListener().getCanonicalName() + " on " + interfaceClass.getCanonicalName() + ":" + method.getName() + " .... : " + e, e);
                    }
                    methodListenerMap.remove(Thread.currentThread().getId() +":"+ method.getName());
                }
            }


//            log.debug("START:invoke:method.getName() = " + method.getName() +" = "+ association + " method.getAnnotation(Default.class) = " + method.getAnnotation(Default.class));
//            if(method.getName().equals("getBitPattern")){
//                log.debug("The trace: ", new Exception("THIS IS DEBUG!!!!!!"));
//            }
//            if(association == null && method.getAnnotation(Default.class) != null){
//                Default annotation = method.getAnnotation(Default.class);
//                DbAttribute dbAttribute = mapOfDbAttributes.get(getAssociationName(method));
//                DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(interfaceClass);
//                dbAttributeContainer.setAttributeValue(object, dbAttribute, annotation.value());
//                association = getAssociation(method);
//                log.debug("SETTING:invoke:method.getName() = " + method.getName() +" = "+ association + " method.getAnnotation(Default.class) = " + method.getAnnotation(Default.class) + " ... association = " + association);
//            }
//			log.debug("END:invoke:method.getName() = " + method.getName() + "->" + association);
			return association;
        } else if(name.startsWith("set") && (objects != null && objects.length == 1)) {
            DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(interfaceClass);
            DbAttribute dbAttribute = dbAttributeContainer.getDbAttribute(getAssociationName(method));
            ModelObjectMethodListener annotation = method.getAnnotation(ModelObjectMethodListener.class);
            ModelObjectMethodListener.MethodListener methodListener = null;
            if(annotation != null && annotation.methodListener() != null){
                if(!methodListenerMap.containsKey(Thread.currentThread().getId() +":"+ method.getName())){
                    methodListenerMap.put(Thread.currentThread().getId() +":"+ method.getName(), "1");
                    try{
                        methodListener = annotation.methodListener().newInstance();
                        methodListener.preRun(object, method.getName(), objects);
                    } catch (Exception e){
                        log.error("Error when calling preRun " + annotation.methodListener().getCanonicalName() + " on " + interfaceClass.getCanonicalName() + ":" + method.getName() + " .... : " + e, e);
                    }
                    methodListenerMap.remove(Thread.currentThread().getId() +":"+ method.getName());
                }
            }
            if(dbAttribute.getAttribute().getAnnotation(Id.class) != null){
                setObjectID("" + objects[0]);
            }
            setAssociation(method, objects[0]);

            if(methodListener != null){
                if(!methodListenerMap.containsKey(Thread.currentThread().getId() +":"+ method.getName())){
                    methodListenerMap.put(Thread.currentThread().getId() +":"+ method.getName(), "1");
                    try{
                        methodListener = annotation.methodListener().newInstance();
                        methodListener.postRun(object, method.getName(), null, objects);
                    } catch (Exception e){
                        log.error("Error when calling postRun " + annotation.methodListener().getCanonicalName() + " on " + interfaceClass.getCanonicalName() + ":" + method.getName() + " .... : " + e, e);
                    }
                    methodListenerMap.remove(Thread.currentThread().getId() +":"+ method.getName());
                }
            }
            return null;
        } else if(name.startsWith("set") && (objects != null && objects.length == 2)) {
            ModelObjectMethodListener annotation = method.getAnnotation(ModelObjectMethodListener.class);
            ModelObjectMethodListener.MethodListener methodListener = null;
            if(annotation != null && annotation.methodListener() != null){
                if(!methodListenerMap.containsKey(Thread.currentThread().getId() +":"+ method.getName())){
                    methodListenerMap.put(Thread.currentThread().getId() +":"+ method.getName(), "1");
                    try{
                        methodListener = annotation.methodListener().newInstance();
                        methodListener.preRun(object, method.getName(), objects);
                    } catch (Exception e){
                        log.error("Error when calling preRun " + annotation.methodListener().getCanonicalName() + " on " + interfaceClass.getCanonicalName() + ":" + method.getName() + " .... : " + e, e);
                    }
                    methodListenerMap.remove(Thread.currentThread().getId() +":"+ method.getName());
                }
            }
            setAssociation(method, objects);

            if(methodListener != null){
                if(!methodListenerMap.containsKey(Thread.currentThread().getId() +":"+ method.getName())){
                    methodListenerMap.put(Thread.currentThread().getId() +":"+ method.getName(), "1");
                    try{
                        methodListener = annotation.methodListener().newInstance();
                        methodListener.postRun(object, method.getName(), null, objects);
                    } catch (Exception e){
                        log.error("Error when calling postRun " + annotation.methodListener().getCanonicalName() + " on " + interfaceClass.getCanonicalName() + ":" + method.getName() + " .... : " + e, e);
                    }
                    methodListenerMap.remove(Thread.currentThread().getId() +":"+ method.getName());
                }
            }
            return null;
        } else {
            throw new RuntimeException("Unknown method: " + name);
        }
    }

    public Object getAssociation(Method method) {
        return getAssociation(getAssociationName(method), method);
    }

    public void setAssociation(Method method, Object object) {
        String name = getAssociationName(method);
        Class<?> parameterClass = method.getParameterTypes()[0];
        if(ModelObjectInterface.class.isAssignableFrom(parameterClass)) {
            setAssociation((ModelObjectInterface) object, name);
        } else if(Enum[].class.isAssignableFrom(parameterClass)) {
            setAssociation((Enum[]) object, name);
        } else if(ModelObjectInterface[].class.isAssignableFrom(parameterClass)) {
            setAssociation((ModelObjectInterface[]) object, name);
        } else if(Calendar.class.isAssignableFrom(parameterClass)) {
            setAssociation((Calendar) object, name);
        } else if(String.class.isAssignableFrom(parameterClass)) {
            String value = null;
            String locale = null;
            if(method.getParameterTypes().length == 1){
                value = (String) object;
            } else {
                value = (String) ((Object[]) object)[0];
                locale = "" +  (((Object[]) object)[1]);
                primitiveAssociationsOfStrings.put(name + "Locale", locale);
            }

            DbStrip dbStrip = method.getAnnotation(DbStrip.class);
            if(dbStrip != null && object != null){
                if(!dbStrip.stripItHard() && !dbStrip.stripItSoft()) {
                    setAssociationWithOutAnyFilters((String) value, name);
                } else if(dbStrip.stripItSoft()){
                    setAssociationWithSoftFilter((String) value, name);
                } else {
                    setAssociation((String) value, name);
                }
            } else {
                setAssociation((String) value, name);
            }
        } else if(Float.class.isAssignableFrom(parameterClass) || Float.TYPE.isAssignableFrom(parameterClass)) {
            setAssociation((Float) object, name);
        } else if(Integer.class.isAssignableFrom(parameterClass) || Integer.TYPE.isAssignableFrom(parameterClass)) {
            setAssociation((Integer) object, name);
        } else if(Long.class.isAssignableFrom(parameterClass) || Long.TYPE.isAssignableFrom(parameterClass)) {
            setAssociation((Long) object, name);
        } else if(Double.class.isAssignableFrom(parameterClass) || Double.TYPE.isAssignableFrom(parameterClass)) {
            setAssociation((Double) object, name);
        } else if(Boolean.class.isAssignableFrom(parameterClass) || Boolean.TYPE.isAssignableFrom(parameterClass)) {
            setAssociation((Boolean) object, name);
        } else if(parameterClass.isEnum()){
            setAssociationWithOutAnyFilters("" + object, name);
        } else {
            throw new RuntimeException("Cannot set attribute " + name + " of type " + parameterClass);
        }
    }

    public static String getAssociationName(Method method) {
        return ClassAnalyser.getAttributeNameFromMethod(method);
    }

    
    private ModelObjectProxy(Class<? extends ModelObjectInterface> interfaceClass) {
        this.interfaceClass = interfaceClass;
        this.doRemoteCache = interfaceClass.getAnnotation(DoRemoteCache.class) != null;
        objectArrayCache = ObjectCacheFactory.getInstance().getObjectArrayCache(this);
        objectCache = ObjectCacheFactory.getInstance().getObjectCache(this);
        if(countOfObjects % 50 == 0){
            log.debug("Creating new ModelObject("+ this.interfaceClass.getName() +")/" + countOfObjects);
        }
        countOfObjects++;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        newPrimaryKey(this.objectID, objectID);
        this.objectID = objectID;
    }

    public String toString() {
        return getPrimaryKeyValue();
    }

    public String getPrimaryKeyValue() {
        return objectID;
    }

    public String getPrimaryKeyName() {
        return "objectID";
    }

    public Class<? extends ModelObjectInterface> getInterface() {
        return interfaceClass;
    }

    public <T extends ModelObjectInterface> T getProxyObject() {
        return (T) proxyObject;
    }

    // THE REST IS COPIED FROM REUSABLE V3

    private transient static final Object THE_NULL_OBJECT = new Object();
    private transient static final Logger log = LoggerFactory.getLogger(ModelObjectProxy.class);
    protected transient Calendar creationDate = Calendar.getInstance();
    protected transient Calendar lastModified = Calendar.getInstance();
    protected transient Calendar lastAccessed = Calendar.getInstance();
    protected transient Calendar expireDate = null;
    protected transient int countOfAccess = 0;

    //protected HashMap savedSingleAssociations = new HashMap();
    protected transient HashMap multiAssociations = new HashMap(); //Only for timeperiode where this object is dirty
    protected transient HashMap<String, Object> singleAssociations = new HashMap<String, Object>(); //Only for timeperiode where this object is dirty
    protected transient HashMap multiAssociationsWithResultEqualToNull = new HashMap();

    protected transient HashMap<String, String> singleAssociationsIDs = new HashMap<String, String>();
    protected transient HashMap primitiveAssociationsOfStrings = new HashMap();
    protected transient HashMap primitiveAssociationsOfCalendars = new HashMap();
    protected transient HashMap primitiveAssociationsOfFloats = new HashMap();
    protected transient HashMap primitiveAssociationsOfIntegers = new HashMap();
    protected transient HashMap primitiveAssociationsOfLongs = new HashMap();
    protected transient HashMap primitiveAssociationsOfDoubles = new HashMap();
    protected transient HashMap primitiveAssociationsOfBooleans = new HashMap();

    protected transient HashMap cachedMultiAssociations = new HashMap();

    protected transient ObjectArrayCache objectArrayCache;
    protected transient ObjectCache objectCache;

    private transient HashMap<String, DbAttribute> mapOfDbAttributes = new HashMap<String, DbAttribute>();

    //public transient static HashMap liveKeys = new HashMap(); // DEBUG
    private static long getAssociationFOUND = 0;
    private static long getAssociationNOT_FOUND = 0;

    private static long countOfObjects = 0;

    /**
     * Is this object a new instance (with a new primary key). If so we must insert a new tupel in the database; if not
     * update an allready exisiting one.
     */

    protected boolean isNew = true;

    /**
     * Is this object dirty (have some of the attributes/fields changed)
     */
    protected boolean isDirty = false;

    /**
     * Can we cache this object (have all attributes been loaded fully).
     */
    protected boolean isCachable = true;

    protected void finalize() throws Throwable {
        super.finalize();
        //log.debug("countOfObjects: " + countOfObjects--);
        multiAssociations.clear();
        singleAssociations.clear();
        multiAssociationsWithResultEqualToNull.clear();

        singleAssociationsIDs.clear();
        primitiveAssociationsOfStrings.clear();
        primitiveAssociationsOfCalendars.clear();
        primitiveAssociationsOfFloats.clear();
        primitiveAssociationsOfIntegers.clear();
        primitiveAssociationsOfLongs.clear();
        primitiveAssociationsOfDoubles.clear();
        primitiveAssociationsOfBooleans.clear();

        cachedMultiAssociations.clear();

        //protected transient ObjectArrayCache objectArrayCache = ObjectCacheFactory.getInstance().getObjectArrayCache(this);
        //protected transient ObjectCache objectCache = ObjectCacheFactory.getInstance().getObjectCache(this);

        mapOfDbAttributes.clear();
    }

    protected Object clone() {
        try {
            ModelObjectProxy c = (ModelObjectProxy) ModelObjectProxy.create(this.interfaceClass);
            c.multiAssociations = (HashMap) this.multiAssociations.clone();
            c.singleAssociations = (HashMap) this.singleAssociations.clone();
            c.singleAssociationsIDs = (HashMap) this.singleAssociationsIDs.clone();
            c.primitiveAssociationsOfStrings = (HashMap) this.primitiveAssociationsOfStrings.clone();
            c.primitiveAssociationsOfCalendars = (HashMap) this.primitiveAssociationsOfCalendars.clone();
            c.primitiveAssociationsOfFloats = (HashMap) this.primitiveAssociationsOfFloats.clone();
            c.primitiveAssociationsOfIntegers = (HashMap) this.primitiveAssociationsOfIntegers.clone();
            c.primitiveAssociationsOfLongs = (HashMap) this.primitiveAssociationsOfLongs.clone();
            c.primitiveAssociationsOfDoubles = (HashMap) this.primitiveAssociationsOfDoubles.clone();
            c.primitiveAssociationsOfBooleans = (HashMap) this.primitiveAssociationsOfBooleans.clone();

            c.mapOfDbAttributes = (HashMap) this.mapOfDbAttributes.clone();
            return c;
        } catch (Exception e) {
            log.error("Fatal error when clone() on " + interfaceClass.getName() + " " + e);
            e.printStackTrace();
            return null;
        }
    }

    public boolean isCachable() {
        return isCachable;
    }

    public void setCachable(boolean isCachable) {
        this.isCachable = isCachable;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean doRemoteCache() {
        return doRemoteCache;
    }

    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    private Object[] getArrayFromCache(String fieldName) {
        ////log.debug("getArrayFromCache called");
        return (Object[]) objectArrayCache.getFromCache(this.getPrimaryKeyValue() + ":" + fieldName);
    }

    public void removeAllListnersForFieldName(String fieldName) {
        //log.debug("removeAllListnersForFieldName start - " + fieldName);
        if(fieldName.startsWith("__")){
            instanceCache.clear();
            return;
        }

        Class arrayClass = (Class) cachedMultiAssociations.get(fieldName);
        if (arrayClass != null) {
            Object[] array = getArrayFromCache(fieldName);
            ObjectCache objectCacheForArrayClass = ObjectCacheFactory.getInstance().getObjectCache(arrayClass);
            if(array != null && array.length > 0 && array[0].getClass().isAssignableFrom(ModelObjectInterface.class)) {
                for (int i = 0; array != null && i < array.length; i++) {
                    //log.debug("removeAllListnersForFieldName loop "+ i +" / "+ array.length);
                    //log.debug("array[i].getPrimaryKeyValue() = "+ array[i].getPrimaryKeyValue());
                    //log.debug("this = " + this);
                    objectCacheForArrayClass.removeListener(((ModelObjectInterface) array[i]).getObjectID(), this, fieldName);
                }
            }
            cachedMultiAssociations.remove(fieldName);
        }
        objectArrayCache.removeFromCache(this.getPrimaryKeyValue() + ":" + fieldName);
        //log.debug("removeAllListnersForFieldName done - " + fieldName);
    }

    public void removeAllListeners() {
        ////log.debug("removeAllListnersForFieldName start");
        for (Iterator ite = cachedMultiAssociations.values().iterator(); ite.hasNext();) {
            String fieldName = (String) ite.next();
            removeAllListnersForFieldName(fieldName);
        }
        ////log.debug("removeAllListnersForFieldName done");
    }

    private void putArrayInCache(ModelObjectInterface[] array, Class arrayClass, String fieldName) {
        ////log.debug("putArrayInCache start -- " + fieldName);
        String arrayID = this.getPrimaryKeyValue() + ":" + fieldName;
        if (array == null || array.length == 0) {
            //multiAssociations.put(fieldName, THE_NULL_OBJECT);
            //multiAssociationsWithResultEqualToNull.put(fieldName, new Object());
            return;
        }

        ObjectCache objectCacheForArrayClass = ObjectCacheFactory.getInstance().getObjectCache(arrayClass);
//        log.debug("objectCacheForArrayClass::"+ fieldName +" for ("+ arrayClass +") is -> " + objectCacheForArrayClass);
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {
                log.error("objectCacheForArrayClass::"+ fieldName +" for ("+ arrayClass +") addListener for objectID("+ array[i] +") ");
            }
            boolean result = objectCacheForArrayClass.addListener(array[i].getObjectID(), this, fieldName);
            if (!result) {
//          log.debug("putArrayInCache: one addListener returned false -> removeListener");
                for (int j = 0; j < i; j++) {
                    objectCacheForArrayClass.removeListener(array[i].getObjectID(), this, fieldName);
                }
                removeAllListnersForFieldName(fieldName);
                return;
            }
        }
        objectArrayCache.putInCache(arrayID, array);
        cachedMultiAssociations.put(fieldName, arrayClass);
        ////log.debug("putArrayInCache end -- " + fieldName);
    }


    private void putArrayInCache(Enum[] array, Class arrayClass, String fieldName) {
        ////log.debug("putArrayInCache start -- " + fieldName);
        String arrayID = this.getPrimaryKeyValue() + ":" + fieldName;
        if (array == null || array.length == 0) {
            //multiAssociations.put(fieldName, THE_NULL_OBJECT);
            //multiAssociationsWithResultEqualToNull.put(fieldName, new Object());
            return;
        }

        objectArrayCache.putInCache(arrayID, array);
        cachedMultiAssociations.put(fieldName, arrayClass);
    }


    protected <T> T getAssociation(String fieldName, Class<T> responseClass) {
        try {
            T toReturn = (T) getAssociation(fieldName);
            if (toReturn == null) {
                if (responseClass.equals(Boolean.class) || responseClass.equals(boolean.class)) {
                    return (T) new Boolean(false);
                } else if (responseClass.equals(Integer.class) || responseClass.equals(int.class)) {
                    return (T) new Integer(0);
                } else if (responseClass.equals(Float.class) || responseClass.equals(float.class)) {
                    return (T) new Float(0);
                } else if (responseClass.equals(Long.class) || responseClass.equals(long.class)) {
                    return (T) new Long(0);
                } else if (responseClass.equals(Double.class) || responseClass.equals(Double.class)) {
                    return (T) new Double(0);
                } else {
                    return null;
                }
            } else {
                return toReturn;
            }
        } catch (Exception e) {
            log.error("FATAL exception in " + interfaceClass.getSimpleName() + "." + fieldName + " ... " + e, e);
            e.printStackTrace();
            throw new Error(e);
        }
    }



    public Object getPrimitiveAssociationsOfStrings(String fieldName) {
        return primitiveAssociationsOfStrings.get(fieldName);
    }
    protected Object getAssociation(String fieldName) {
        return getAssociation(fieldName, (Method) null);
    }



    public ArrayIsNullResult isArrayNull(String fieldName){
        Object toReturn = multiAssociations.get(fieldName);
        if (toReturn != null) {
            if (toReturn.equals(THE_NULL_OBJECT)) {
                return ArrayIsNullResult.YES_IS_NULL;
            } else {
                return ArrayIsNullResult.NO_NOT_NULL;
            }
        }
        if (multiAssociationsWithResultEqualToNull.containsKey(fieldName)) {
            return ArrayIsNullResult.YES_IS_NULL;
        }

        toReturn = getArrayFromCache(fieldName);
        if (toReturn == null) {
            return ArrayIsNullResult.DONT_KNOW;
        } else {
            return ((Object[])toReturn).length > 0 ? ArrayIsNullResult.NO_NOT_NULL : ArrayIsNullResult.YES_IS_NULL;
        }
    }


    protected Object getAssociation(String fieldName, Method method) {
//        String debugMessage = "getAssociation:(" + getAssociationFOUND + " / " + getAssociationNOT_FOUND + "):" + this + ":" + interfaceClass.getName() + "." + fieldName;
//        log.debug(debugMessage + "::START");

        DbAttribute dbAttribute = (DbAttribute) mapOfDbAttributes.get(fieldName);
        if (dbAttribute == null) {
            DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(interfaceClass);
            dbAttribute = dbAttributeContainer.getDbAttribute(fieldName);
            mapOfDbAttributes.put(fieldName, dbAttribute);
        }
        Object toReturn = null;
        if (dbAttribute.isMultiAssociation()) {
//            log.debug("getAssociation ...:: getting multiAssociation - start");
            toReturn = multiAssociations.get(fieldName);
            if (toReturn != null) {
//                log.debug(debugMessage + "::found in cache::ENDS");
                getAssociationFOUND++;
                if (toReturn.equals(THE_NULL_OBJECT)) {
                    return null;
                } else {
                    return toReturn;
                }
            }
            if (multiAssociationsWithResultEqualToNull.containsKey(fieldName)) {
//                log.debug(debugMessage + "::found in cache::ENDS");
                getAssociationFOUND++;
                return null;
            }

            toReturn = getArrayFromCache(fieldName);
            if (toReturn == null && !dbAttribute.getAttributeClass().isEnum()) {
//                log.debug(debugMessage + "::NOT FOUND in cache::ENDS");
                getAssociationNOT_FOUND++;
                DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(interfaceClass);
                toReturn = DbObjectReader.getMultiAssociation(this, dbAttributeContainer, dbAttribute, new HashMap(), new AssociationConstrain(), "", true);
//                log.debug("getAssociation("+ fieldName +") toReturn :: " + (toReturn != null ? ((Object[] )toReturn).length : -1));
                if(toReturn != null && ((Object[]) toReturn).length == 0){
                    toReturn = null;
                } else if(toReturn != null) {
                    putArrayInCache((ModelObjectInterface[]) toReturn, dbAttribute.getAttributeClass(), fieldName);
                }
            }
            if (toReturn == null || ((Object[]) toReturn).length == 0) {
                multiAssociationsWithResultEqualToNull.put(fieldName, new Object());
            } else if (multiAssociationsWithResultEqualToNull.containsKey(fieldName)) {
                multiAssociationsWithResultEqualToNull.remove(fieldName);
            }

//            log.debug("getAssociation :: getting multiAssociation - done :: " + (toReturn != null ? ((Object[]) toReturn).length : -1));
        } else if (dbAttribute.isAssociation()) {
//            log.debug("getAssociation :: getting singleAssociationsIDs - start");
            toReturn = singleAssociations.get(fieldName);
            if (toReturn != null) {
//                log.debug("getAssociation :: found in hashMap of associations");
                return (toReturn.equals(THE_NULL_OBJECT) ? null : toReturn);
            }
            String associationPrimaryKey = (String) singleAssociationsIDs.get(fieldName);
            if (associationPrimaryKey != null && !associationPrimaryKey.equals("null")) {
//                log.debug("getAssociation :: getting singleAssociationsIDs - has hashKey " + associationPrimaryKey);
                toReturn = DbObjectReader.readObjectFromDb(associationPrimaryKey, dbAttribute.getAttributeClass());
                // TODO don't forget singleAssociations.put here
            } else {
//                log.debug("getAssociation :: getting singleAssociation - DONT HAVE hashKey " + fieldName + " in " + this);
            }
//            log.debug("getAssociation :: getting singleAssociation - done " + toReturn);
        } else if (dbAttribute.getAttributeClass().equals(String.class)) {
            toReturn = primitiveAssociationsOfStrings.get(fieldName);
        } else if (dbAttribute.getAttributeClass().equals(boolean.class)) {
            toReturn = primitiveAssociationsOfBooleans.get(fieldName);
        } else if (dbAttribute.getAttributeClass().equals(long.class)) {
            toReturn = primitiveAssociationsOfLongs.get(fieldName);
        } else if (dbAttribute.getAttributeClass().equals(int.class)) {
            toReturn = primitiveAssociationsOfIntegers.get(fieldName);
        } else if (dbAttribute.getAttributeClass().equals(double.class)) {
            toReturn = primitiveAssociationsOfDoubles.get(fieldName);
        } else if (dbAttribute.getAttributeClass().equals(float.class)) {
            toReturn = primitiveAssociationsOfFloats.get(fieldName);
        } else if (dbAttribute.getAttributeClass().equals(Calendar.class)) {
            toReturn = primitiveAssociationsOfCalendars.get(fieldName);
        } else if (dbAttribute.getAttributeClass().isEnum()) {
            String value = (String) primitiveAssociationsOfStrings.get(fieldName);
            if(value == null || value.equalsIgnoreCase("null")){
                toReturn = null;
            } else {
                Object[] enumConstants = dbAttribute.getAttributeClass().getEnumConstants();
                for(int i = 0; enumConstants != null && i < enumConstants.length; i++){
                    if(enumConstants[i].toString().equals(value)){
                        toReturn = enumConstants[i];
                    }
                }
            }
        } else {
            log.error("Some ERROR in getAssociation() :: " + fieldName + " " + interfaceClass.getName() + " ... extends from ModelObject?");
        }
        //if ((dbAttribute.getAttributeClass().isPrimitive() || isNew() && (dbAttribute.getAttributeClass() == String.class || dbAttribute.getAttributeClass() == Calendar.class)) && toReturn == null) {
        if ((dbAttribute.getAttributeClass().isPrimitive() || (dbAttribute.getAttributeClass() == String.class || dbAttribute.getAttributeClass() == Calendar.class)) && toReturn == null) {
            toReturn = primitiveDefaultValue(dbAttribute.getAttributeClass(), method);
        }

        return toReturn;
    }

    private Object primitiveDefaultValue(Class clazz, Method method) {
        //if(method != null && isNew() && method.getAnnotation(Default.class) != null) {
        if(method != null && method.getAnnotation(Default.class) != null) {
            String textValue = method.getAnnotation(Default.class).value();
            return PropertiesProxy.parse(textValue, method.getReturnType(), interfaceClass.getSimpleName() + "." + method.getName());
        }





        if (clazz.equals(boolean.class)) {
            return false;
        } else if (clazz.equals(char.class)) {
            return (char)0;
        } else if (clazz.equals(byte.class)) {
            return (byte)0;
        } else if (clazz.equals(short.class)) {
            return (short)0;
        } else if (clazz.equals(int.class)) {
            return (int)0;
        } else if (clazz.equals(float.class)) {
            return (float)0f;
        } else if (clazz.equals(long.class)) {
            return (long)0;
        } else if (clazz.equals(double.class)) {
            return (double)0;
        } else {
            return null;
        }
    }

    public boolean containsAssociationInCache(String fieldName) {
        DbAttribute dbAttribute = (DbAttribute) mapOfDbAttributes.get(fieldName);
        if (dbAttribute == null) {
            DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(interfaceClass);
            dbAttribute = dbAttributeContainer.getDbAttribute(fieldName);
            mapOfDbAttributes.put(fieldName, dbAttribute);
        }
        Object toReturn = null;
        if (dbAttribute.isMultiAssociation()) {
            ////log.debug("getAssociation ...:: getting multiAssociation - start");
            toReturn = multiAssociations.get(fieldName);
            if (toReturn != null) {
                if (toReturn.equals(THE_NULL_OBJECT)) {
                    return true;
                } else {
                    return true;
                }
            }
            if (multiAssociationsWithResultEqualToNull.containsKey(fieldName)) {
                return true;
            }

            toReturn = getArrayFromCache(fieldName);
            if (toReturn == null) {
                return false;
            }
        } else if (dbAttribute.isAssociation()) {
            ////log.debug("getAssociation :: getting singleAssociationsIDs - start");
            toReturn = singleAssociations.get(fieldName);
            if (toReturn != null) {
                ////log.debug("getAssociation :: found in hashMap of associations");
                return true;
            }
            String associationPrimaryKey = (String) singleAssociationsIDs.get(fieldName);
            return (associationPrimaryKey != null && ObjectCacheFactory.getInstance().getObjectCache(dbAttribute.getAttributeClass()).getFromCache(associationPrimaryKey) != null);
        } else if (dbAttribute.getAttributeClass().equals(String.class)) {
            return true;
        } else if (dbAttribute.getAttributeClass().equals(Calendar.class)) {
            return true;
        } else {
            log.error("Some ERROR in getAssociation() :: " + fieldName + " " + interfaceClass.getName());
        }
        ////log.debug("getAssociation :: returns " + toReturn);
        return false;
    }


//    public void copyNotNullValuesIntoMe(ModelObjectInterface copyFromInterface) {
//        ModelObjectProxy copyFrom = (ModelObjectProxy) copyFromInterface;
//        for (Iterator iterator = copyFrom.primitiveAssociationsOfStrings.keySet().iterator(); iterator.hasNext();) {
//            Object key = iterator.next();
//            Object s = copyFrom.primitiveAssociationsOfStrings.get(key);
//            if (s != null && !s.equals(THE_NULL_OBJECT)) {
//                setAssociationWithOutAnyFilters((String) copyFrom.primitiveAssociationsOfStrings.get(key), (String) key);
//            }
//        }
//        for (Iterator iterator = copyFrom.primitiveAssociationsOfCalendars.keySet().iterator(); iterator.hasNext();) {
//            Object key = iterator.next();
//            Object s = copyFrom.primitiveAssociationsOfCalendars.get(key);
//            if (s != null && !s.equals(THE_NULL_OBJECT)) {
//                setAssociation((Calendar) copyFrom.primitiveAssociationsOfCalendars.get(key), (String) key);
//            }
//        }
//        for (Iterator iterator = copyFrom.primitiveAssociationsOfFloats.keySet().iterator(); iterator.hasNext();) {
//            Object key = iterator.next();
//            Object s = copyFrom.primitiveAssociationsOfFloats.get(key);
//            if (s != null && !s.equals(THE_NULL_OBJECT)) {
//                setAssociation((Float) copyFrom.primitiveAssociationsOfFloats.get(key), (String) key);
//            }
//        }
//        for (Iterator iterator = copyFrom.primitiveAssociationsOfIntegers.keySet().iterator(); iterator.hasNext();) {
//            Object key = iterator.next();
//            Object s = copyFrom.primitiveAssociationsOfIntegers.get(key);
//            if (s != null && !s.equals(THE_NULL_OBJECT)) {
//                setAssociation((Integer) copyFrom.primitiveAssociationsOfIntegers.get(key), (String) key);
//            }
//        }
//        for (Iterator iterator = copyFrom.primitiveAssociationsOfLongs.keySet().iterator(); iterator.hasNext();) {
//            Object key = iterator.next();
//            Object s = copyFrom.primitiveAssociationsOfLongs.get(key);
//            if (s != null && !s.equals(THE_NULL_OBJECT)) {
//                setAssociation((Long) copyFrom.primitiveAssociationsOfLongs.get(key), (String) key);
//            }
//        }
//        for (Iterator iterator = copyFrom.primitiveAssociationsOfDoubles.keySet().iterator(); iterator.hasNext();) {
//            Object key = iterator.next();
//            Object s = copyFrom.primitiveAssociationsOfDoubles.get(key);
//            if (s != null && !s.equals(THE_NULL_OBJECT)) {
//                setAssociation((Double) copyFrom.primitiveAssociationsOfDoubles.get(key), (String) key);
//            }
//        }
//        for (Iterator iterator = copyFrom.primitiveAssociationsOfBooleans.keySet().iterator(); iterator.hasNext();) {
//            Object key = iterator.next();
//            Object s = copyFrom.primitiveAssociationsOfBooleans.get(key);
//            if (s != null && !s.equals(THE_NULL_OBJECT)) {
//                setAssociation((Boolean) copyFrom.primitiveAssociationsOfBooleans.get(key), (String) key);
//            }
//        }
//        for (Iterator iterator = copyFrom.singleAssociations.keySet().iterator(); iterator.hasNext();) {
//            Object key = iterator.next();
//            Object s = copyFrom.singleAssociations.get(key);
//            if (s != null && !s.equals(THE_NULL_OBJECT)) {
//                setAssociation((ModelObject) copyFrom.singleAssociations.get(key), (String) key);
//            }
//        }
//        for (Iterator iterator = copyFrom.multiAssociations.keySet().iterator(); iterator.hasNext();) {
//            Object key = iterator.next();
//            Object s = copyFrom.multiAssociations.get(key);
//            if (s != null && !s.equals(THE_NULL_OBJECT)) {
//                setAssociation((ModelObject[]) copyFrom.multiAssociations.get(key), (String) key);
//            }
//        }
//    }


    public String toDebugString(String delim) {
        StringBuilder s = new StringBuilder(15);
        s.append("ID(" + getPrimaryKeyValue() + ") super.toString("+ super.toString() +")");
        for (Iterator iterator = primitiveAssociationsOfStrings.keySet().iterator(); iterator.hasNext();) {
            Object key = iterator.next();
            s.append(key);
            s.append('=');
            s.append(primitiveAssociationsOfStrings.get(key));
            s.append(delim);
        }
        for (Iterator iterator = primitiveAssociationsOfCalendars.keySet().iterator(); iterator.hasNext();) {
            Object key = iterator.next();
            s.append(key);
            s.append('=');
            Calendar calendar = (Calendar) primitiveAssociationsOfCalendars.get(key);
            if (calendar != null) {
                s.append(calendar.getTime());
            } else {
                s.append("null");
            }
            s.append(delim);
        }
        for (Iterator iterator = singleAssociations.keySet().iterator(); iterator.hasNext();) {
            Object key = iterator.next();
            s.append(key);
            s.append('=');
            s.append(singleAssociations.get(key));
            s.append(delim);
        }
        return s.toString();
    }


    public void setSingleAssociationID(String fieldName, String objectID) {
        singleAssociationsIDs.put(fieldName, objectID);
    }

    protected void removeAssociation(String fieldName) {
        singleAssociationsIDs.remove(fieldName);
    }

    protected void setAssociation(ModelObjectInterface object, String fieldName) {
        setAssociation((ModelObject) object, fieldName);
    }

//    protected void setAssociation(ModelObjectInterface[] object, String fieldName) {
//        setAssociation((ModelObject[]) object, fieldName);
//    }

    protected void setAssociation(ModelObject object, String fieldName) {
        if (!isDirty && !isNew) {
            makesDirtyForAssociation((ModelObject) getAssociation(fieldName), object, fieldName);
        }
        singleAssociationsIDs.put(fieldName, "" + object);
//        if (isDirty || isNew) {
//            singleAssociations.put(fieldName, (object == null ? THE_NULL_OBJECT : object));
//        }
        if (object == null || isNew || object.isNew()) {
            singleAssociations.put(fieldName, (object == null ? THE_NULL_OBJECT : object));
        } else {
            singleAssociations.remove(fieldName);
        }
        //TODO: Should only add if the assObject isNew - else remove fieldName from map


    }

    protected void setAssociation(ModelObjectInterface[] object, String fieldName) {
        if (object != null && object.length == 0) {
            object = null;
        }
        //log.debug("setAssociation :: multiAssociation :: setting " + fieldName + " isDirty = " + isDirty);
        if (multiAssociationsWithResultEqualToNull.containsKey(fieldName) && object != null && object.length > 0) {
            isDirty = true;
        }
        if (!isDirty && !isNew) {
            makesDirtyForAssociation((ModelObjectInterface[]) getAssociation(fieldName), object, fieldName);
        }
        ////log.debug("setAssociation :: multiAssociation :: setting " + fieldName + " isDirty = " + isDirty);
        if (object == null) {
            multiAssociationsWithResultEqualToNull.put(fieldName, new Object());
        } else if (multiAssociationsWithResultEqualToNull.containsKey(fieldName)) {
            multiAssociationsWithResultEqualToNull.remove(fieldName);
        }
        if (isDirty || isNew) {
            multiAssociations.put(fieldName, (object == null ? THE_NULL_OBJECT : object));
            //removeAllListnersForFieldName(fieldName);
            //objectArrayCache.removeFromCache(this.getPrimaryKeyValue()+":"+fieldName);
        } else {
        }
        DbAttribute dbAttribute = (DbAttribute) mapOfDbAttributes.get(fieldName);
        if (dbAttribute == null) {
            DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(interfaceClass);
            dbAttribute = dbAttributeContainer.getDbAttribute(fieldName);
            mapOfDbAttributes.put(fieldName, dbAttribute);
        }
        putArrayInCache(object, dbAttribute.getAttributeClass(), fieldName);

    }


    protected void setAssociation(Enum[] object, String fieldName) {
        if (object != null && object.length == 0) {
            object = null;
        }
        //log.debug("setAssociation :: multiAssociation :: setting " + fieldName + " isDirty = " + isDirty);
        if (multiAssociationsWithResultEqualToNull.containsKey(fieldName) && object != null && object.length > 0) {
            isDirty = true;
        }
        if (!isDirty && !isNew) {
            makesDirtyForAssociation((Enum[]) getAssociation(fieldName), object, fieldName);
        }
        ////log.debug("setAssociation :: multiAssociation :: setting " + fieldName + " isDirty = " + isDirty);
        if (object == null) {
            multiAssociationsWithResultEqualToNull.put(fieldName, new Object());
        } else if (multiAssociationsWithResultEqualToNull.containsKey(fieldName)) {
            multiAssociationsWithResultEqualToNull.remove(fieldName);
        }
        if (isDirty || isNew) {
            multiAssociations.put(fieldName, (object == null ? THE_NULL_OBJECT : object));
            //removeAllListnersForFieldName(fieldName);
            //objectArrayCache.removeFromCache(this.getPrimaryKeyValue()+":"+fieldName);
        } else {
        }
        DbAttribute dbAttribute = (DbAttribute) mapOfDbAttributes.get(fieldName);
        if (dbAttribute == null) {
            DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(interfaceClass);
            dbAttribute = dbAttributeContainer.getDbAttribute(fieldName);
            mapOfDbAttributes.put(fieldName, dbAttribute);
        }
        putArrayInCache(object, dbAttribute.getAttributeClass(), fieldName);

    }


//    protected void setAssociation(String[] object, String fieldName) {
//        if (object != null && object.length == 0) {
//            object = null;
//        }
//        //log.debug("setAssociation :: multiAssociation :: setting " + fieldName + " isDirty = " + isDirty);
//        if (multiAssociationsWithResultEqualToNull.containsKey(fieldName) && object != null && object.length > 0) {
//            isDirty = true;
//        }
//        if (!isDirty && !isNew) {
//            makesDirtyForAssociation((String[]) getAssociation(fieldName), object, fieldName);
//        }
//        ////log.debug("setAssociation :: multiAssociation :: setting " + fieldName + " isDirty = " + isDirty);
//        if (object == null) {
//            multiAssociationsWithResultEqualToNull.put(fieldName, new Object());
//        } else if (multiAssociationsWithResultEqualToNull.containsKey(fieldName)) {
//            multiAssociationsWithResultEqualToNull.remove(fieldName);
//        }
//        if (isDirty || isNew) {
//            multiAssociations.put(fieldName, (object == null ? THE_NULL_OBJECT : object));
//            //removeAllListnersForFieldName(fieldName);
//            //objectArrayCache.removeFromCache(this.getPrimaryKeyValue()+":"+fieldName);
//        } else {
//        }
//        DbAttribute dbAttribute = (DbAttribute) mapOfDbAttributes.get(fieldName);
//        if (dbAttribute == null) {
//            DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(interfaceClass);
//            dbAttribute = dbAttributeContainer.getDbAttribute(fieldName);
//            mapOfDbAttributes.put(fieldName, dbAttribute);
//        }
//        putArrayInCache(object, dbAttribute.getAttributeClass(), fieldName);
//
//    }

    protected void setAssociation(Calendar object, String fieldName) {
        makesDirtyForAssociation((Calendar) getAssociation(fieldName), object, fieldName);
        primitiveAssociationsOfCalendars.put(fieldName, object);
    }

    protected void setAssociation(Float object, String fieldName) {
        makesDirtyForAssociation((Float) getAssociation(fieldName), object, fieldName);
        primitiveAssociationsOfFloats.put(fieldName, object);
    }

    protected void setAssociation(Integer object, String fieldName) {
        makesDirtyForAssociation((Integer) getAssociation(fieldName), object, fieldName);
        primitiveAssociationsOfIntegers.put(fieldName, object);
    }

    protected void setAssociation(Boolean object, String fieldName) {
        makesDirtyForAssociation((Boolean) getAssociation(fieldName), object, fieldName);
        primitiveAssociationsOfBooleans.put(fieldName, object);
    }

    protected void setAssociation(Long object, String fieldName) {
        makesDirtyForAssociation((Long) getAssociation(fieldName), object, fieldName);
        primitiveAssociationsOfLongs.put(fieldName, object);
    }

    protected void setAssociation(Double object, String fieldName) {
        makesDirtyForAssociation((Double) getAssociation(fieldName), object, fieldName);
        primitiveAssociationsOfDoubles.put(fieldName, object);
    }

    protected void setAssociation(String object, String fieldName) {
        String filter = charFilter(setFirstCharacter(object));
        makesDirtyForAssociation((String) getAssociation(fieldName), filter, fieldName);
        primitiveAssociationsOfStrings.put(fieldName, filter);
    }

    protected void setAssociationWithOutAnyFilters(String object, String fieldName) {
        Object association = getAssociation(fieldName);  //Because of Enums
        makesDirtyForAssociation((association == null ? null : "" + association), object, fieldName);
        primitiveAssociationsOfStrings.put(fieldName, object);
    }

    protected void setAssociationWithOutFilter(String object, String fieldName) {
        makesDirtyForAssociation((String) getAssociation(fieldName), object, fieldName);
        primitiveAssociationsOfStrings.put(fieldName, object);
    }

    protected void setAssociationWithSoftFilter(String object, String fieldName) {
        object = charFilterSoft(object);
        makesDirtyForAssociation((String) getAssociation(fieldName), object, fieldName);
        primitiveAssociationsOfStrings.put(fieldName, object);
    }

    protected void setAssociationWithSoftFilterAndUpperCase(String object, String fieldName) {
        object = charFilterSoft(object);
        makesDirtyForAssociation((String) getAssociation(fieldName), object, fieldName);
        primitiveAssociationsOfStrings.put(fieldName, object);
    }

    protected void setAssociationWithSoftNamesFilter(String object, String fieldName) {
        object = setFirstCharacterSoft(charFilterSoftNames(object));
        makesDirtyForAssociation((String) getAssociation(fieldName), object, fieldName);
        primitiveAssociationsOfStrings.put(fieldName, object);
    }

    protected void setAssociationWithOutUpperCase(String object, String fieldName) {
        makesDirtyForAssociation((String) getAssociation(fieldName), charFilter(object), fieldName);
        primitiveAssociationsOfStrings.put(fieldName, charFilter(object));
    }


    private boolean makesDirtyForAssociation(ModelObjectInterface obj1, ModelObjectInterface obj2, String fieldName) {
        if (!isDirty && !isNew) {
            if (obj1 == null) {
                if (obj2 != null) {
                    isDirty = true;
                }
            } else if (obj2 == null) {
                isDirty = true;
            } else {
//                if (!obj1.equals(obj2)) {
                if (! (obj1 == obj2 || ((ModelObject) obj1).equalsRef((ModelObject) obj2))) { // Compare by reference! See ModelReferenceTest
                    isDirty = true;
                }
            }
        }
        return isDirty;
    }

    private boolean makesDirtyForAssociation(String obj1, String obj2, String fieldName) {
        if (!isDirty && !isNew) {
            if (obj1 == null) {
                if (obj2 != null) {
                    isDirty = true;
                }
            } else if (obj2 == null) {
                isDirty = true;
            } else {
                if (!obj1.equals(obj2)) {
                    isDirty = true;
                }
            }
        }
        return isDirty;
    }


    private void makesDirtyForAssociation(ModelObjectInterface[] obj1, ModelObjectInterface[] obj2, String fieldName) {

        if (!isDirty && !isNew) {
            if (obj1 == null) {
                if (obj2 != null) {
                    isDirty = true;
                }
            } else if (obj2 == null) {
                isDirty = true;
            } else {
                if (obj1.length != obj2.length) {
                    isDirty = true;
                } else {
                    for (int i = 0; i < obj1.length; i++) {
                        if (makesDirtyForAssociation((ModelObject) obj1[i], (ModelObject) obj2[i], fieldName)) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private void makesDirtyForAssociation(Enum[] obj1, Enum[] obj2, String fieldName) {

        if (!isDirty && !isNew) {
            if (obj1 == null) {
                if (obj2 != null) {
                    isDirty = true;
                }
            } else if (obj2 == null) {
                isDirty = true;
            } else {
                if (obj1.length != obj2.length) {
                    isDirty = true;
                } else {
                    List<Enum> enums = Arrays.asList(obj2);
                    for (int i = 0; i < obj1.length; i++) {
                        enums.remove(obj1[i]);
                    }
                    if(!enums.isEmpty()){
                        isDirty = true;
                    }
                }
            }
        }
    }

    private void makesDirtyForAssociation(String[] obj1, String[] obj2, String fieldName) {

        if (!isDirty && !isNew) {
            if (obj1 == null) {
                if (obj2 != null) {
                    isDirty = true;
                }
            } else if (obj2 == null) {
                isDirty = true;
            } else {
                if (obj1.length != obj2.length) {
                    isDirty = true;
                } else {
                    for (int i = 0; i < obj1.length; i++) {
                        if (makesDirtyForAssociation((String) obj1[i], (String) obj2[i], fieldName)) {
                            break;
                        }
                    }
                }
            }
        }
    }


    protected void makesDirtyForAssociation(Calendar obj1, Calendar obj2, String fieldName) {
        if (!isDirty && !isNew) {
            if (obj1 == null) {
                if (obj2 != null) {
                    isDirty = true;
                }
            } else if (obj2 == null) {
                isDirty = true;
            } else {
                if (obj1.getTimeInMillis() != obj2.getTimeInMillis()) {
                    isDirty = true;
                }
            }
        }
    }

//  public void makesDirty(int value1, int value2) {
//    if (value1 != value2) {
//      isDirty = true;
//    }
//  }

    public void makesDirty() {
        isDirty = true;
    }

//  public void makesDirty(double value1, double value2) {
//    if (value1 != value2) {
//      isDirty = true;
//    }
//  }
//
//  public void makesDirty(float value1, float value2) {
//    if (value1 != value2) {
//      isDirty = true;
//    }
//  }
//
//    public void makesDirty(boolean value1, boolean value2) {
//      if (value1 != value2) {
//        isDirty = true;
//      }
//    }

    public void makesDirtyForAssociation(Boolean value1, Boolean value2, String fieldName) {
        isDirty = isDirty || (value1 == null && value2 != null) || (value1 != null && value2 == null) || !value1.equals(value2);
    }

    public void makesDirtyForAssociation(Integer value1, Integer value2, String fieldName) {
        isDirty = isDirty || (value1 == null && value2 != null) || (value1 != null && value2 == null) || !value1.equals(value2);
    }

    public void makesDirtyForAssociation(Double value1, Double value2, String fieldName) {
        isDirty = isDirty || (value1 == null && value2 != null) || (value1 != null && value2 == null) || !value1.equals(value2);
    }

    public void makesDirtyForAssociation(Float value1, Float value2, String fieldName) {
        isDirty = isDirty || (value1 == null && value2 != null) || (value1 != null && value2 == null) || !value1.equals(value2);
    }

    public void makesDirtyForAssociation(Long value1, Long value2, String fieldName) {
        isDirty = isDirty || (value1 == null && value2 != null) || (value1 != null && value2 == null) || !value1.equals(value2);
    }


    /**
     * Determines if we are changing the primary key; and the object should be new.
     */
    public void newPrimaryKey(String value1, String value2) {
        if (value1 != null && value2 != null && !value1.equals(value2)) {
            isNew = true;
        }
    }

    public int compareTo(Object obj) {
        return this.getPrimaryKeyValue().compareTo(obj.toString());
    }

    /**
     * Implementes the equals - metode.
     */
    public boolean equalsRef(ModelObject obj) {
        return obj != null && obj.getProxyObject() == proxyObject;
    }

    public boolean equals(Object obj) {
        return obj != null && obj.toString().equals(this.toString());
    }

    public static ModelObject[] concatArrays(
            ModelObject[] array1,
            ModelObject[] array2) {

        if (array1 == null || array1.length == 0) {
            return array2;
        }

        if (array2 == null || array2.length == 0) {
            return array1;
        }

        ArrayList arrayList = new ArrayList();

        for (int i = 0; i < array1.length; i++) {
            if (!arrayList.contains(array1[i])) {
                arrayList.add(array1[i]);
            }
        }

        for (int i = 0; i < array2.length; i++) {
            if (!arrayList.contains(array2[i])) {
                arrayList.add(array2[i]);
            }
        }

        return (ModelObject[]) arrayList.toArray((Object[]) java.lang.reflect.Array.newInstance(
                array1[0].getClass(),
                arrayList.size()));
    }

    public static ModelObject[] innerJoin(ModelObject[] array1,
                                          ModelObject[] array2) {
        if (array1 == null || array1.length == 0 || array2 == null || array2.length == 0) {
            return null;
        }
        ArrayList toReturn = new ArrayList();
        for (int i = 0; i < array1.length; i++) {
            boolean found = false;
            for (int j = 0; !found && j < array2.length; j++) {
                found = array1[i].equals(array2[j]);
            }
            if (found) {
                toReturn.add(array1[i]);
            }
        }
        return (ModelObject[]) toReturn.toArray((Object[]) java.lang.reflect.Array.newInstance(
                array1[0].getClass(),
                toReturn.size()));

    }

    public static boolean isModelObjectInArray(
            ModelObject[] modelObjects,
            ModelObject modelObject) {
        for (int i = 0; modelObjects != null && i < modelObjects.length; i++) {
            if (modelObjects[i] != null
                    && modelObjects[i].equals(modelObject)) {
                return true;
            }
        }
        return false;
    }


    public <T extends ModelObjectInterface> T[] addObjectToArray(T[] objectArray, T objToAdd) {
        makesDirty();
//      System.out.println("addObjectToArray making: " + objToAdd.getClass());
        if (objectArray != null && objectArray.length > 0) {
            boolean isAllReadyInArray = false;
            for (int i = 0; i < objectArray.length; i++) {
                if (objectArray[i].equals(objToAdd)) {
                    isAllReadyInArray = true;
                }
            }
            if (!isAllReadyInArray) {
                ModelObjectInterface[] newObjectArray =
                        (ModelObjectInterface[]) java.lang.reflect.Array.newInstance(
                                ((ModelObject) objToAdd).getInterface(),
                                objectArray.length + 1);
                System.arraycopy(objectArray, 0, newObjectArray, 0, objectArray.length);
                newObjectArray[objectArray.length] = objToAdd;
                return (T[]) newObjectArray;
            }
            return objectArray;
        } else {
            ////log.debug("addObjectToArray - 2");
            ModelObjectInterface[] newObjectArray =
                    (ModelObjectInterface[]) java.lang.reflect.Array.newInstance(
                            ((ModelObject) objToAdd).getInterface(),
                            1);
            newObjectArray[0] = objToAdd;
            ////log.debug("addObjectToArray - newObjectArray.length = " + newObjectArray.length);
            return (T[]) newObjectArray;
        }
    }

    public <T extends ModelObjectInterface> T[] removeObjectFromArray(T[] objectArray, T objToRemove) {
        makesDirty();
        ////log.debug("removeObjectFromArray :: start");
        if (objectArray == null || objectArray.length == 0) {
            ////log.debug("removeObjectFromArray :: ends-1");
            return null;
        }

        boolean isInArray = false;
        for (int i = 0; i < objectArray.length; i++) {
            if (objectArray[i].equals(objToRemove)) {
                isInArray = true;
            }
        }
        if (!isInArray) {
            ////log.debug("removeObjectFromArray :: ends-2");
            return objectArray;

        }
        if (objectArray.length == 1) {
            ////log.debug("removeObjectFromArray :: ends-3");
            return null;
        } else {
            ModelObjectInterface[] newObjectArray =
                    (ModelObjectInterface[]) java.lang.reflect.Array.newInstance(
                            ((ModelObject) objToRemove).getInterface(),
                            objectArray.length - 1);
            int j = 0;
            for (int i = 0; i < objectArray.length; i++) {
                if (!objectArray[i].equals(objToRemove)) {
                    newObjectArray[j++] = objectArray[i];
                }
            }
            ////log.debug("removeObjectFromArray :: ends-4");
            return (T[]) newObjectArray;
        }
    }

    public ModelObjectInterface[] removeObjectFromArray(ModelObjectInterface[] objectArray, String id) {
        if (objectArray == null || objectArray.length == 0) {
            return null;
        }
        boolean isInArray = false;
        for (int i = 0; i < objectArray.length; i++) {
            if (objectArray[i].toString().equals(id)) {
                isInArray = true;
            }
        }
        if (!isInArray) {
            return objectArray;

        }
        makesDirty();
        if (objectArray.length == 1 && objectArray[0].toString().equals(id)) {
            return null;
        } else {
            ModelObjectInterface[] newObjectArray = (ModelObjectInterface[])
                    java.lang.reflect.Array.newInstance(
                            ((ModelObject) objectArray[0]).getInterface(), objectArray.length - 1);
            int j = 0;
            for (int i = 0; i < objectArray.length; i++) {
                if (!objectArray[i].toString().equals(id)) {
                    newObjectArray[j++] = objectArray[i];
                }
            }
            ////log.debug("removeObjectFromArray :: ends-4");
            return newObjectArray;
        }
    }


    public Calendar getCreationDate() {
        //System.out.println("hallo " + primitiveAssociationsOfCalendars);
        Object o = primitiveAssociationsOfCalendars.get("creationDate");
        if (o == null) {
            return creationDate;
        }
        return (Calendar) o;
    }

    public void setCreationDate(Calendar creationDate) {
        setAssociation(creationDate, "creationDate");
    }

//    public Calendar getLastModified() {
//        Calendar lastMo = (Calendar) primitiveAssociationsOfCalendars.get("lastModified");
//        if (lastMo == null) {
//            return lastModified;
//        }
//        return lastMo;
//    }
//
//    public void setLastModified(Calendar lastModified) {
//        primitiveAssociationsOfCalendars.put("lastModified", lastModified);
//        //Because we dont want this object to be written, because of lastModified ..
//    }
//
//    public Calendar getLastAccessed() {
//        Object association = primitiveAssociationsOfCalendars.get("lastAccessed");
//        if (association == null) {
//            return lastAccessed;
//        }
//        return (Calendar) association;
//    }
//
//    public void setLastAccessed(Calendar lastAccessed) {
//        //Have to be empty !!!
//    }
//
//    public Calendar getExpireDate() {
//        return (Calendar) primitiveAssociationsOfCalendars.get("expireDate");
//    }
//
//    public void setExpireDate(Calendar expireDate) {
//        setAssociation(expireDate, "expireDate");
//    }


    public static String setFirstCharacter(String name) {
        if (name != null && !name.isEmpty()) {
            name = (name.substring(0, 1)).toUpperCase() + name.substring(1);
        }
        return name;
    }

    public static String setFirstCharacterSoft(String name) {
        if (name != null && !name.isEmpty()) {
            if (!name.startsWith("der ") && !name.startsWith("van ") && !name.startsWith("te ") && !name.startsWith("in ") &&
                    !name.startsWith("ter ") && !name.startsWith("ten ") && !name.startsWith("vd ") &&
                    !name.startsWith("v.d. ") && !name.startsWith("v.d ") && !name.startsWith("v/d ") &&
                    !name.startsWith("p/a ") && !name.startsWith("op ") && !name.startsWith("a/b ") && !name.startsWith("a/b ")) {
                name = (name.substring(0, 1)).toUpperCase() + name.substring(1);
            }
        }
        return name;
    }
//...
    public static String charFilter(String name) {
        if (name != null) {
            return charFilterSoft(name).replaceAll("/|&|'|<|>|;|\\\\", "");
        }
        return name;
    }


    public static void main(String[] args) {
        System.out.println(charFilterSoft("fuck\\asfd"));
    }

    public static String charFilterSoft(String name) {
        if (name != null) {
            name = name.replaceAll("[^\\u0000-\\u02B8\\u0390-\\u057F]", "");
            return name.replaceAll("'|\"|\\\\", "`");
        }
        return name;
    }

    public static String charFilterSoftNames(String name) {
        if (name != null) {
            return name.replaceAll("'|\"", "`").replaceAll("'|\"|<|>|;", "");
        }
        return name;
    }

    public static String[] charFilter(String[] names) {
        if (names != null && names.length > 0) {
            for (int i = 0; i < names.length; i++) {
                names[i] = charFilter(names[i]);
            }
        }
        return names;
    }

    public void save() {
        ////log.debug("saving - start");
//        System.out.println("ModelObjectProxy.save() : " + this + " / " + this.getClass());
        DbObjectWriter.writeObjectToDb(this);
        ////log.debug("saving - end");
    }

    public void doneSavingByDbObjectWriter() {
        singleAssociations.clear();
        multiAssociations.clear();
        instanceCache.clear();
    }




    Map<String, Pair<Long, Object>> instanceCache = new HashMap<String, Pair<Long, Object>>(20);
    @Override
    public void putInInstanceCache(String key, Object object, int secondsToLive) {
        long newTime = System.currentTimeMillis() + secondsToLive * 1000;
        instanceCache.put(key, new Pair<Long, Object>(newTime, object));

        if(object instanceof ModelObject){
            ObjectCacheFactory.getInstance().getObjectCache(object).addListener("" + objectID, this, "__instanceCache");
        }
    }

    public Object getFromInstanceCache(String key) {
        Pair<Long, Object> longObjectPair = instanceCache.get(key);
        if(longObjectPair == null) {
            return null;
        } else {
            Long first = longObjectPair.getFirst();
            if(System.currentTimeMillis() < first){
                return longObjectPair.getSecond();
            } else {
                removeFromInstanceCache(key);
                return null;
            }
        }
    }

    @Override
    public void removeFromInstanceCache(String key) {
        instanceCache.remove(key);
    }

    public boolean delete() {
        objectCache.removeFromCache("" + this);
        AssociationConstrain associationConstrain = new AssociationConstrain();
        associationConstrain.setDontAllowAnyAssociations();
        return DbObjectDeleter.deleteObjectFromDb(
                this.getPrimaryKeyValue(),
                interfaceClass,
                new ArrayList(),
                associationConstrain,
                "");
    }

    public boolean deleteWithAssociations(String commaStringOfAttributeNames) {
        objectCache.removeFromCache("" + this);
        ////log.debug("deleteWithAssociations : " + 1);
        AssociationConstrain associationConstrain = new AssociationConstrain();
        associationConstrain.setAssociationConstrainType(
                AssociationConstrain.ALLOW_ASSOCIATIONS_IN_LIST);
        ////log.debug("deleteWithAssociations : " + 2);
        associationConstrain.addAssociation("");
        StringTokenizer stringTokenizer =
                new StringTokenizer(commaStringOfAttributeNames, ",");
        ////log.debug("deleteWithAssociations : " + 3);
        while (stringTokenizer.hasMoreTokens()) {
            associationConstrain.addAssociation(stringTokenizer.nextToken());
        }
        ////log.debug("deleteWithAssociations : " + 4 + " associationConstrain " + associationConstrain);
        boolean toReturn =
                DbObjectDeleter.deleteObjectFromDb(
                        this.getPrimaryKeyValue(),
                        interfaceClass,
                        new ArrayList(),
                        associationConstrain,
                        "");
        ////log.debug("deleteWithAssociations : toReturn" + toReturn);
        return toReturn;
    }

    public boolean deleteWithOutAssociations(String commaStringOfAttributeNames) {
        objectCache.removeFromCache("" + this);
        AssociationConstrain associationConstrain = new AssociationConstrain();
        associationConstrain.setAssociationConstrainType(
                AssociationConstrain.DONT_ALLOW_ASSOCIATIONS_IN_LIST);
        StringTokenizer stringTokenizer =
                new StringTokenizer(commaStringOfAttributeNames, ",");
        while (stringTokenizer.hasMoreTokens()) {
            associationConstrain.addAssociation(stringTokenizer.nextToken());
        }
        return DbObjectDeleter.deleteObjectFromDb(
                this.getPrimaryKeyValue(),
                interfaceClass,
                new ArrayList(),
                associationConstrain,
                "");
    }


    public int hashCode() {
        return getPrimaryKeyValue().hashCode();
    }

    public String getSingleAssociationID(String attributeName) {
        Object toReturn = singleAssociations.get(attributeName);
        if (toReturn != null) {
            return (toReturn.equals(THE_NULL_OBJECT) ? null : ((ModelObject) toReturn).getPrimaryKeyValue());
        }
        String associationPrimaryKey = singleAssociationsIDs.get(attributeName);

        return associationPrimaryKey;
    }
}
