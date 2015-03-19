package dk.lessismore.nojpa.db.statements.oracle;

import dk.lessismore.nojpa.guid.GuidFactory;
import dk.lessismore.nojpa.properties.Default;
import dk.lessismore.nojpa.properties.PropertiesProxy;
import dk.lessismore.nojpa.reflection.db.*;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttribute;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttributeContainer;
import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class OracleDataProxy  { //implements dk.lessismore.nojpa.db.statements.oracle.OracleDataProxyInterface, InvocationHandler {

//    private transient static final Logger log = Logger.getLogger(dk.lessismore.nojpa.db.statements.oracle.OracleDataProxy.class);
//
//
//    private final Class<?> interfaceClass;
//    private Object proxyObject;
//
//
//    private OracleDataProxy(Class<?> interfaceClass) {
//        this.interfaceClass = interfaceClass;
//    }
//
//
//
//    public static <T> T create(Class<T> interfaceClass) {
//        dk.lessismore.nojpa.db.statements.oracle.OracleDataProxy object = new dk.lessismore.nojpa.db.statements.oracle.OracleDataProxy(interfaceClass);
//        object.proxyObject = Proxy.newProxyInstance(
//                interfaceClass.getClassLoader(),
//                new Class<?>[]{interfaceClass, dk.lessismore.nojpa.db.statements.oracle.OracleDataProxyInterface.class},
//                object);
//        T toReturn = (T) object.proxyObject;
//        return toReturn;
//    }
//
//
//
//    @Override
//    public Object invoke(Object proxy, Method method, Object[] objects) throws Throwable {
//        String name = method.getName();
//        log.debug("Calling " + name);
//        if(method.getDeclaringClass().equals(Object.class)) {
//            return method.invoke(this, objects);
//        } else if(method.getDeclaringClass().equals(OracleDataProxyInterface.class)){
//            return method.invoke(this, objects);
//        } else if(name.startsWith("get")) {
//            Object association = getAssociation(method);
//            return association;
//        } else if(name.startsWith("set")) {
//            setAssociation(method, objects[0]);
//            return null;
//        } else {
//            throw new RuntimeException("Unknown method: " + name);
//        }
//    }
//
//
//    private String objectID = GuidFactory.getInstance().makeGuid();
//    private final static HashMap<String, String> methodListenerMap = new HashMap<String, String>();
//
//    private Object getAssociation(Method method) {
//        return getAssociation(getAssociationName(method), method);
//    }
//
//    @Override
//    public void setAssociation(Method method, Object object) {
//        String name = getAssociationName(method);
//        Class<?> parameterClass = method.getParameterTypes()[0];
////        if(ModelObjectInterface.class.isAssignableFrom(parameterClass)) {
////            setAssociation((ModelObjectInterface) object, name);
////        } else if(ModelObjectInterface[].class.isAssignableFrom(parameterClass)) {
////            setAssociation((ModelObjectInterface[]) object, name);
////        } else
//
//        if(Calendar.class.isAssignableFrom(parameterClass)) {
//            setAssociation((Calendar) object, name);
//        } else if(String.class.isAssignableFrom(parameterClass)) {
//            String value = null;
//            String locale = null;
//            if(method.getParameterTypes().length == 1){
//                value = (String) object;
//            } else {
//                value = (String) ((Object[]) object)[0];
//                locale = "" +  (((Object[]) object)[1]);
//                primitiveAssociationsOfStrings.put(name + "Locale", locale);
//            }
//            setAssociation((String) value, name);
//        } else if(Float.class.isAssignableFrom(parameterClass) || Float.TYPE.isAssignableFrom(parameterClass)) {
//            setAssociation((Float) object, name);
//        } else if(Integer.class.isAssignableFrom(parameterClass) || Integer.TYPE.isAssignableFrom(parameterClass)) {
//            setAssociation((Integer) object, name);
//        } else if(Long.class.isAssignableFrom(parameterClass) || Long.TYPE.isAssignableFrom(parameterClass)) {
//            setAssociation((Long) object, name);
//        } else if(Double.class.isAssignableFrom(parameterClass) || Double.TYPE.isAssignableFrom(parameterClass)) {
//            setAssociation((Double) object, name);
//        } else if(Boolean.class.isAssignableFrom(parameterClass) || Boolean.TYPE.isAssignableFrom(parameterClass)) {
//            setAssociation((Boolean) object, name);
//        } else if(parameterClass.isEnum()){
////            setAssociationWithOutAnyFilters("" + object, name);
//            setAssociation("" + object, name);
//        } else {
//            throw new RuntimeException("Cannot set attribute " + name + " of type " + parameterClass);
//        }
//    }
//
//    public static String getAssociationName(Method method) {
//        return method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
//    }
//
//
//
//    public String getObjectID() {
//        return objectID;
//    }
//
//    public void setObjectID(String objectID) {
//        this.objectID = objectID;
//    }
//
//    public String toString() {
//        return getPrimaryKeyValue();
//    }
//
//    public String getPrimaryKeyValue() {
//        return objectID;
//    }
//
//    public String getPrimaryKeyName() {
//        return "objectID";
//    }
//
//
//    public <T> T getProxyObject() {
//        return (T) proxyObject;
//    }
//
//
//    private transient static final Object THE_NULL_OBJECT = new Object();
//    protected transient Calendar creationDate = Calendar.getInstance();
//
//    protected transient HashMap multiAssociations = new HashMap(); //Only for timeperiode where this object is dirty
//    protected transient HashMap<String, Object> singleAssociations = new HashMap<String, Object>(); //Only for timeperiode where this object is dirty
//    protected transient HashMap multiAssociationsWithResultEqualToNull = new HashMap();
//
//    protected transient HashMap<String, String> singleAssociationsIDs = new HashMap<String, String>();
//    protected transient HashMap primitiveAssociationsOfStrings = new HashMap();
//    protected transient HashMap primitiveAssociationsOfCalendars = new HashMap();
//    protected transient HashMap primitiveAssociationsOfFloats = new HashMap();
//    protected transient HashMap primitiveAssociationsOfIntegers = new HashMap();
//    protected transient HashMap primitiveAssociationsOfLongs = new HashMap();
//    protected transient HashMap primitiveAssociationsOfDoubles = new HashMap();
//    protected transient HashMap primitiveAssociationsOfBooleans = new HashMap();
//
//    protected transient HashMap cachedMultiAssociations = new HashMap();
//
//
//    private transient HashMap<String, DbAttribute> mapOfDbAttributes = new HashMap<String, DbAttribute>();
//
//
//
//    protected void finalize() throws Throwable {
//        super.finalize();
//        //log.debug("countOfObjects: " + countOfObjects--);
//        multiAssociations.clear();
//        singleAssociations.clear();
//        multiAssociationsWithResultEqualToNull.clear();
//
//        singleAssociationsIDs.clear();
//        primitiveAssociationsOfStrings.clear();
//        primitiveAssociationsOfCalendars.clear();
//        primitiveAssociationsOfFloats.clear();
//        primitiveAssociationsOfIntegers.clear();
//        primitiveAssociationsOfLongs.clear();
//        primitiveAssociationsOfDoubles.clear();
//        primitiveAssociationsOfBooleans.clear();
//
//        cachedMultiAssociations.clear();
//
//        //protected transient ObjectArrayCache objectArrayCache = ObjectCacheFactory.getInstance().getObjectArrayCache(this);
//        //protected transient ObjectCache objectCache = ObjectCacheFactory.getInstance().getObjectCache(this);
//
//        mapOfDbAttributes.clear();
//    }
//
//    protected Object clone() {
//        try {
//            dk.lessismore.nojpa.db.statements.oracle.OracleDataProxy c = (dk.lessismore.nojpa.db.statements.oracle.OracleDataProxy) dk.lessismore.nojpa.db.statements.oracle.OracleDataProxy.create(this.interfaceClass);
//            c.multiAssociations = (HashMap) this.multiAssociations.clone();
//            c.singleAssociations = (HashMap) this.singleAssociations.clone();
//            c.singleAssociationsIDs = (HashMap) this.singleAssociationsIDs.clone();
//            c.primitiveAssociationsOfStrings = (HashMap) this.primitiveAssociationsOfStrings.clone();
//            c.primitiveAssociationsOfCalendars = (HashMap) this.primitiveAssociationsOfCalendars.clone();
//            c.primitiveAssociationsOfFloats = (HashMap) this.primitiveAssociationsOfFloats.clone();
//            c.primitiveAssociationsOfIntegers = (HashMap) this.primitiveAssociationsOfIntegers.clone();
//            c.primitiveAssociationsOfLongs = (HashMap) this.primitiveAssociationsOfLongs.clone();
//            c.primitiveAssociationsOfDoubles = (HashMap) this.primitiveAssociationsOfDoubles.clone();
//            c.primitiveAssociationsOfBooleans = (HashMap) this.primitiveAssociationsOfBooleans.clone();
//
//            c.mapOfDbAttributes = (HashMap) this.mapOfDbAttributes.clone();
//            return c;
//        } catch (Exception e) {
//            log.error("Fatal error when clone() on " + interfaceClass.getName() + " " + e);
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//
//    protected <T> T getAssociation(String fieldName, Class<T> responseClass) {
//        try {
//            T toReturn = (T) getAssociation(fieldName);
//            if (toReturn == null) {
//                if (responseClass.equals(Boolean.class) || responseClass.equals(boolean.class)) {
//                    return (T) new Boolean(false);
//                } else if (responseClass.equals(Integer.class) || responseClass.equals(int.class)) {
//                    return (T) new Integer(0);
//                } else if (responseClass.equals(Float.class) || responseClass.equals(float.class)) {
//                    return (T) new Float(0);
//                } else if (responseClass.equals(Long.class) || responseClass.equals(long.class)) {
//                    return (T) new Long(0);
//                } else if (responseClass.equals(Double.class) || responseClass.equals(Double.class)) {
//                    return (T) new Double(0);
//                } else {
//                    return null;
//                }
//            } else {
//                return toReturn;
//            }
//        } catch (Exception e) {
//            log.error("FATAL exception in " + interfaceClass.getSimpleName() + "." + fieldName + " ... " + e, e);
//            e.printStackTrace();
//            throw new Error(e);
//        }
//    }
//
//
//
//    public Object getPrimitiveAssociationsOfStrings(String fieldName) {
//        return primitiveAssociationsOfStrings.get(fieldName);
//    }
//    protected Object getAssociation(String fieldName) {
//        return getAssociation(fieldName, (Method) null);
//    }
//
//
//    protected Object getAssociation(String fieldName, Method method) {
////        String debugMessage = "getAssociation:(" + getAssociationFOUND + " / " + getAssociationNOT_FOUND + "):" + this + ":" + interfaceClass.getName() + "." + fieldName;
////        log.debug(debugMessage + "::START");
//
//        Object toReturn = null;
//        if (method.getReturnType().equals(String.class)) {
//            toReturn = primitiveAssociationsOfStrings.get(fieldName);
//        } else if (method.getReturnType().equals(boolean.class)) {
//            toReturn = primitiveAssociationsOfBooleans.get(fieldName);
//        } else if (method.getReturnType().equals(long.class)) {
//            toReturn = primitiveAssociationsOfLongs.get(fieldName);
//        } else if (method.getReturnType().equals(int.class)) {
//            toReturn = primitiveAssociationsOfIntegers.get(fieldName);
//        } else if (method.getReturnType().equals(double.class)) {
//            toReturn = primitiveAssociationsOfDoubles.get(fieldName);
//        } else if (method.getReturnType().equals(float.class)) {
//            toReturn = primitiveAssociationsOfFloats.get(fieldName);
//        } else if (method.getReturnType().equals(Calendar.class)) {
//            toReturn = primitiveAssociationsOfCalendars.get(fieldName);
//        } else if (method.getReturnType().isEnum()) {
//            String value = (String) primitiveAssociationsOfStrings.get(fieldName);
//            if(value == null || value.equalsIgnoreCase("null")){
//                toReturn = null;
//            } else {
//                Object[] enumConstants = method.getReturnType().getEnumConstants();
//                for(int i = 0; enumConstants != null && i < enumConstants.length; i++){
//                    if(enumConstants[i].toString().equals(value)){
//                        toReturn = enumConstants[i];
//                    }
//                }
//            }
//        }
//        if ((method.getReturnType().isPrimitive() || (method.getReturnType() == String.class || method.getReturnType() == Calendar.class)) && toReturn == null) {
//            toReturn = primitiveDefaultValue(method.getReturnType(), method);
//        }
//
//        return toReturn;
//    }
//
//    private Object primitiveDefaultValue(Class clazz, Method method) {
//        //if(method != null && isNew() && method.getAnnotation(Default.class) != null) {
//        if(method != null && method.getAnnotation(Default.class) != null) {
//            String textValue = method.getAnnotation(Default.class).value();
//            return PropertiesProxy.parse(textValue, method.getReturnType(), interfaceClass.getSimpleName() + "." + method.getName());
//        }
//        if (clazz.equals(boolean.class)) {
//            return false;
//        } else if (clazz.equals(char.class)) {
//            return (char)0;
//        } else if (clazz.equals(byte.class)) {
//            return (byte)0;
//        } else if (clazz.equals(short.class)) {
//            return (short)0;
//        } else if (clazz.equals(int.class)) {
//            return (int)0;
//        } else if (clazz.equals(float.class)) {
//            return (float)0f;
//        } else if (clazz.equals(long.class)) {
//            return (long)0;
//        } else if (clazz.equals(double.class)) {
//            return (double)0;
//        } else {
//            return null;
//        }
//    }
//
//
//    public String toDebugString(String delim) {
//        StringBuilder s = new StringBuilder(15);
//        s.append("ID(" + getPrimaryKeyValue() + ") super.toString("+ super.toString() +")");
//        for (Iterator iterator = primitiveAssociationsOfStrings.keySet().iterator(); iterator.hasNext();) {
//            Object key = iterator.next();
//            s.append(key);
//            s.append('=');
//            s.append(primitiveAssociationsOfStrings.get(key));
//            s.append(delim);
//        }
//        for (Iterator iterator = primitiveAssociationsOfCalendars.keySet().iterator(); iterator.hasNext();) {
//            Object key = iterator.next();
//            s.append(key);
//            s.append('=');
//            Calendar calendar = (Calendar) primitiveAssociationsOfCalendars.get(key);
//            if (calendar != null) {
//                s.append(calendar.getTime());
//            } else {
//                s.append("null");
//            }
//            s.append(delim);
//        }
//        for (Iterator iterator = singleAssociations.keySet().iterator(); iterator.hasNext();) {
//            Object key = iterator.next();
//            s.append(key);
//            s.append('=');
//            s.append(singleAssociations.get(key));
//            s.append(delim);
//        }
//        return s.toString();
//    }
//
//
//    public void setSingleAssociationID(String fieldName, String objectID) {
//        singleAssociationsIDs.put(fieldName, objectID);
//    }
//
//    protected void removeAssociation(String fieldName) {
//        singleAssociationsIDs.remove(fieldName);
//    }
//
//    protected void setAssociation(ModelObjectInterface object, String fieldName) {
//        setAssociation((ModelObject) object, fieldName);
//    }
//
//
//    protected void setAssociation(ModelObject object, String fieldName) {
//        singleAssociationsIDs.put(fieldName, "" + object);
//        singleAssociations.put(fieldName, (object == null ? THE_NULL_OBJECT : object));
//    }
//
//    protected void setAssociation(Object[] object, String fieldName) {
//        if (object != null && object.length == 0) {
//            object = null;
//        }
//        if (object == null) {
//            multiAssociationsWithResultEqualToNull.put(fieldName, new Object());
//        } else if (multiAssociationsWithResultEqualToNull.containsKey(fieldName)) {
//            multiAssociationsWithResultEqualToNull.remove(fieldName);
//        }
//        multiAssociations.put(fieldName, (object == null ? THE_NULL_OBJECT : object));
//        DbAttribute dbAttribute = (DbAttribute) mapOfDbAttributes.get(fieldName);
//        if (dbAttribute == null) {
//            DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(interfaceClass);
//            dbAttribute = dbAttributeContainer.getDbAttribute(fieldName);
//            mapOfDbAttributes.put(fieldName, dbAttribute);
//        }
//    }
//
//
//    protected void setAssociation(Calendar object, String fieldName) {
//        primitiveAssociationsOfCalendars.put(fieldName, object);
//    }
//
//    protected void setAssociation(Float object, String fieldName) {
//        primitiveAssociationsOfFloats.put(fieldName, object);
//    }
//
//    protected void setAssociation(Integer object, String fieldName) {
//        primitiveAssociationsOfIntegers.put(fieldName, object);
//    }
//
//    protected void setAssociation(Boolean object, String fieldName) {
//        primitiveAssociationsOfBooleans.put(fieldName, object);
//    }
//
//    protected void setAssociation(Long object, String fieldName) {
//        primitiveAssociationsOfLongs.put(fieldName, object);
//    }
//
//    protected void setAssociation(Double object, String fieldName) {
//        primitiveAssociationsOfDoubles.put(fieldName, object);
//    }
//
//    protected void setAssociation(String object, String fieldName) {
//        primitiveAssociationsOfStrings.put(fieldName, object);
//    }
//
//
//
//
//
//
//
//    public int compareTo(Object obj) {
//        return this.getPrimaryKeyValue().compareTo(obj.toString());
//    }
//
//    /**
//     * Implementes the equals - metode.
//     */
//    public boolean equals(Object obj) {
//        return obj != null && obj.toString().equals(this.toString());
//    }
//
//
//
//
//
//
//
//
//    public int hashCode() {
//        return getPrimaryKeyValue().hashCode();
//    }
//
//
//

}


