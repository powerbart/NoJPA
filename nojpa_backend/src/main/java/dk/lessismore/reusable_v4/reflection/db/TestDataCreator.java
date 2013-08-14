package dk.lessismore.reusable_v4.reflection.db;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObject;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectProxy;

import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Fills in bogus test data for objects.
 * If you ask it to create an object (some subtype of ModelObjectInterface) it will fill in its setters and create new
 * model objects to fill in the setters that requires those. It stops at some depth or after a set number of creations.
 */
public class TestDataCreator {
    private final int maxCount;
    private final int maxDepth;
    private final Random random = new Random();
    private final Set<String> ignoreSet;
    private int count;
    private static final Map<Class<? extends ModelObjectInterface>, ModelObjectInterface[]> objectMap =
            new HashMap<Class<? extends ModelObjectInterface>, ModelObjectInterface[]>();

    /**
     * The modelClass is a class (or interface) all the concrete objects inherits from.
     * It is examined for setters (such as setObjectID) and these are ignored when filling in data later on.
     */
    public TestDataCreator(Class<? extends ModelObjectInterface> modelClass) {
        this(modelClass, 3, 100);
    }

    /**
     * The modelClass is a class (or interface) all the concrete objects inherits from.
     * It is examined for setters and these are ignored when filling in data later on.
     * It will not create new instances beyond the specified depth and no more new instances than the specified count.
     */
    public TestDataCreator(Class<? extends ModelObjectInterface> modelClass, int maxDepth, int maxCount) {
        this.maxDepth = maxDepth;
        this.maxCount = maxCount;
        this.ignoreSet = new HashSet<String>();
        for(Method method: modelClass.getMethods()) {
            this.ignoreSet.add(method.getName());
        }
    }

    /**
     * Sets a pool for a given model object interface. It clears any existing pool for that particular interface.
     * It will draw nested objects from this pool instead of creating new instances.
     * If the array is empty (no objects specified), it will use null for setters that expect the type.
     */
    public <T extends ModelObjectInterface> void setPoolFor(Class<T> objectInterface, T... objects) {
        if(objects == null) {
            removePoolFor(objectInterface);
        } else {
            objectMap.put(objectInterface, objects);
        }
    }

    /**
     * Remove the object pool for the specified type.
     */
    public void removePoolFor(Class<? extends ModelObjectInterface> objectInterface) {
        objectMap.remove(objectInterface);
    }

    /**
     * Creates a bogus test object (saved in the database). String fields have a special format; for example,
     * OrderLine.getAxPurchID() will return "OL.axPurchID_A4EB0" where A4EB0 is the last part of its objectID.
     * It will create other test objects to fill in the setters that take model objects or arrays of them as arguments. 
     */
    public <T extends ModelObjectInterface> T create(Class<T> objectInterface) {
        count = 0;
        return createModel(objectInterface, 0);
    }

    private <T extends ModelObjectInterface> T createModel(Class<T> objectInterface, int depth) {
        if(count > maxCount || depth > maxDepth) return null;
        count += 1;
        try {
            T object = ModelObjectProxy.create(objectInterface);
            String classPrefix = objectInterface.getSimpleName().replaceAll("[a-z]", "");
            // TODO object.getClass() should replaced with ((ModelObject)object).getInterface()
            for(Method method: object.getClass().getMethods()) {
                String name = method.getName();
                if(name.startsWith("set") && !ignoreSet.contains(name) && method.getParameterTypes().length == 1) {
                    String methodSuffix = name.substring(3, 4).toLowerCase() + name.substring(4);
                    Class parameterClass = method.getParameterTypes()[0];
                    if(parameterClass.isAssignableFrom(String.class)) {
                        String objectSuffix = object.getObjectID().substring(object.getObjectID().length() - 5);
                        method.invoke(object, classPrefix + "." + methodSuffix + "_" + objectSuffix);
                    } else if(parameterClass.isArray() || parameterClass.isPrimitive() ||
                            ModelObjectInterface.class.isAssignableFrom(parameterClass) ||
                            Date.class.isAssignableFrom(parameterClass) ||
                            Calendar.class.isAssignableFrom(parameterClass)) {
                        Object value = createAny(parameterClass, depth + 1);
                        if(value != null) method.invoke(object, value);
                    } else {
                        throw new RuntimeException(parameterClass.getSimpleName() + "." + method.getName());
                    }
                }
            }
            ((ModelObject) object).save();
            return object;
        } catch(IllegalAccessException e) {
            throw new RuntimeException(objectInterface.getSimpleName(), e);
        } catch(InstantiationException e) {
            throw new RuntimeException(objectInterface.getSimpleName(), e);
        } catch(InvocationTargetException e) {
            throw new RuntimeException(objectInterface.getSimpleName(), e);
        }
    }

    private <T> T createAny(Class<T> anyClass, int depth) throws InstantiationException, IllegalAccessException {
        if(anyClass.isAssignableFrom(String.class)) {
            return (T) ("Foo" + random.nextInt(1000));
        } else if(Date.class.isAssignableFrom(anyClass)) {
            return (T) new Date();
        } else if(Calendar.class.isAssignableFrom(anyClass)) {
            return (T) new GregorianCalendar();
        } else if(ModelObjectInterface.class.isAssignableFrom(anyClass)) {
            for(Map.Entry<Class<? extends ModelObjectInterface>, ModelObjectInterface[]> entry: objectMap.entrySet()) {
                if(entry.getKey().isAssignableFrom(anyClass)) {
                    if(entry.getValue().length == 0) return null;
                    return (T) entry.getValue()[random.nextInt(entry.getValue().length)];
                }
            }
            return (T) createModel(ModelObjectInterface.class.getClass().cast(anyClass), depth + 1);
        } else if(anyClass.isArray()) {
            Class componentClass = anyClass.getComponentType();
            if(count > maxCount || depth > maxDepth) return (T) Array.newInstance(componentClass, 0);
            Object value1 = createAny(componentClass, depth + 1);
            Object value2 = createAny(componentClass, depth + 1);
            Object array = Array.newInstance(componentClass,
                    value1 != null ? (value2 != null && value1 != value2 ? 2 : 1) : 0);
            if(value1 != null) Array.set(array, 0, value1);
            if(value2 != null && value1 != value2) Array.set(array, 1, value2);
            return (T) array;
        } else {
            return createPrimitive(anyClass);
        }
    }

    private <T> T createPrimitive(Class<T> anyClass) {
        if(anyClass.isAssignableFrom(Short.class) || anyClass.isAssignableFrom(Short.TYPE)) {
            return (T) (Short) (short) random.nextInt(64);
        } else if(anyClass.isAssignableFrom(Integer.class) || anyClass.isAssignableFrom(Integer.TYPE)) {
            return (T) (Integer) random.nextInt(64);
        } else if(anyClass.isAssignableFrom(Long.class) || anyClass.isAssignableFrom(Long.TYPE)) {
            return (T) (Long) (long) random.nextInt(64);
        } else if(anyClass.isAssignableFrom(Byte.class) || anyClass.isAssignableFrom(Byte.TYPE)) {
            return (T) (Byte) (byte) random.nextInt(64);
        } else if(anyClass.isAssignableFrom(Character.class) || anyClass.isAssignableFrom(Character.TYPE)) {
            return (T) (Character) '_';
        } else if(anyClass.isAssignableFrom(Double.class) || anyClass.isAssignableFrom(Double.TYPE)) {
            return (T) (Double) random.nextDouble();
        } else if(anyClass.isAssignableFrom(Float.class) || anyClass.isAssignableFrom(Float.TYPE)) {
            return (T) (Float) random.nextFloat();
        } else if(anyClass.isAssignableFrom(Boolean.class) || anyClass.isAssignableFrom(Boolean.TYPE)) {
            return (T) (Boolean) random.nextBoolean();
        } else {
            throw new RuntimeException(anyClass.getSimpleName());
        }
    }
}
