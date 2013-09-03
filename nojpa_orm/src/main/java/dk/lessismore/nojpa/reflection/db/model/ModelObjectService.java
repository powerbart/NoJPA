package dk.lessismore.nojpa.reflection.db.model;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.reflection.db.annotations.ModelObjectLifeCycleListener;
import dk.lessismore.nojpa.reflection.util.ReflectionUtil;

import java.util.ArrayList;

public class ModelObjectService {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ModelObjectService.class);



    /** Copies the fields that aren't null from the second argument into the first. */
    public static <T extends ModelObjectInterface> void copyPresentFields(T target, T source) {
//        ModelObject t = ((ModelObject) target).getProxyObject();
//        ModelObject s = ((ModelObject) source).getProxyObject();
//        t.copyNotNullValuesIntoMe(s);
        ReflectionUtil.copyNotNulls(source, target);
    }

    /** Creates a debug friendly string representation of the object. */
    public static String toDebugString(ModelObjectInterface object) {
        return ((ModelObject) object).toDebugString(";");
    }

    /** Creates a new model object implementing the given interface. */
    public static <T extends ModelObjectInterface> T create(Class<T> interfaceClass) {
        T t = ModelObjectProxy.create(interfaceClass);
        ModelObjectLifeCycleListener annotation = interfaceClass.getAnnotation(ModelObjectLifeCycleListener.class);
        if(annotation != null && annotation.lifeCycleListener() != null){
            try {
                ModelObjectLifeCycleListener.LifeCycleListener lifeCycleListener = annotation.lifeCycleListener().newInstance();
                lifeCycleListener.onNew(t);
            } catch (Exception e) {
                log.error("Some Exception when creating and running LifeCycleListener onNew() " + interfaceClass.getCanonicalName() + " ... " + e, e);
            }

        }
        return t;
    }

    public static <T extends ModelObjectInterface> void save(T object) {
        ModelObjectLifeCycleListener annotation = ((ModelObject) object).getInterface().getAnnotation(ModelObjectLifeCycleListener.class);
        if(annotation != null && annotation.lifeCycleListener() != null){
            try {
                ModelObjectLifeCycleListener.LifeCycleListener lifeCycleListener = annotation.lifeCycleListener().newInstance();
                lifeCycleListener.preUpdate(object);
            } catch (Exception e) {
                log.error("Some Exception when creating and running LifeCycleListener on preUpdate("+ object +")" + ((ModelObject) object).getInterface().getCanonicalName() + " ... " + e, e);
            }

        }
        ((ModelObject) object).save();
        if(annotation != null && annotation.lifeCycleListener() != null){
            try {
                ModelObjectLifeCycleListener.LifeCycleListener lifeCycleListener = annotation.lifeCycleListener().newInstance();
                lifeCycleListener.postUpdate(object);
            } catch (Exception e) {
                log.error("Some Exception when creating and running LifeCycleListener on postUpdate("+ object +")" + ((ModelObject) object).getInterface().getCanonicalName() + " ... " + e, e);
            }

        }
    }

    public static <T extends ModelObjectInterface> void delete(T object) {
        if (object != null) {
            ModelObjectLifeCycleListener annotation = ((ModelObject) object).getInterface().getAnnotation(ModelObjectLifeCycleListener.class);
            if(annotation != null && annotation.lifeCycleListener() != null){
                try {
                    ModelObjectLifeCycleListener.LifeCycleListener lifeCycleListener = annotation.lifeCycleListener().newInstance();
                    lifeCycleListener.onDelete(object);
                } catch (Exception e) {
                    log.error("Some Exception when creating and running LifeCycleListener on preUpdate("+ object +")" + ((ModelObject) object).getInterface().getCanonicalName() + " ... " + e, e);
                }
            }
            ((ModelObject) object).delete();
        }
    }

    public static void delete(Class<? extends ModelObjectInterface> moiClass, String objectID) {
        ModelObjectInterface moi = MQL.selectByID(moiClass, objectID);
        delete(moi);
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


    public static <T extends ModelObjectInterface> T[] addObjectToArray(T[] objectArray, T objToAdd) {

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

    public static <T extends ModelObjectInterface> T[] removeObjectFromArray(T[] objectArray, T objToRemove) {
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

    public static ModelObjectInterface[] removeObjectFromArray(ModelObjectInterface[] objectArray, String id) {
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



}
