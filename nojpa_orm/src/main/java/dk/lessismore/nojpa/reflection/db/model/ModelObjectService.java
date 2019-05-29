package dk.lessismore.nojpa.reflection.db.model;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.reflection.ClassReflector;
import dk.lessismore.nojpa.reflection.attributes.AttributeContainer;
import dk.lessismore.nojpa.reflection.db.annotations.ModelObjectLifeCycleListener;
import dk.lessismore.nojpa.reflection.util.ReflectionUtil;
import dk.lessismore.nojpa.utils.MaxSizeArray;
import dk.lessismore.nojpa.utils.MaxSizeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ModelObjectService {

    private static final Logger log = LoggerFactory.getLogger(ModelObjectService.class);



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
        if(annotation != null && annotation.lifeCycleListener() != null && (object.isNew() || object.isDirty())){
            try {
                ModelObjectLifeCycleListener.LifeCycleListener lifeCycleListener = annotation.lifeCycleListener().newInstance();
                lifeCycleListener.preUpdate(object);
            } catch (Exception e) {
                log.error("Some Exception when creating and running LifeCycleListener on preUpdate("+ object +")" + ((ModelObject) object).getInterface().getCanonicalName() + " ... " + e, e);
            }

        }
        ((ModelObject) object).save();
        if(annotation != null && annotation.lifeCycleListener() != null && (object.isNew() || object.isDirty())){
            try {
                ModelObjectLifeCycleListener.LifeCycleListener lifeCycleListener = annotation.lifeCycleListener().newInstance();
                lifeCycleListener.postUpdate(object);
            } catch (Exception e) {
                log.error("Some Exception when creating and running LifeCycleListener on postUpdate("+ object +")" + ((ModelObject) object).getInterface().getCanonicalName() + " ... " + e, e);
            }

        }
    }




    protected static class SaveLaterQueue {

        Thread lazyThread = null;
        private final MaxSizeArray<ModelObjectInterface> objectsToSave = new MaxSizeArray<>(50);

        public SaveLaterQueue(){
            lazyThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    while (true){
                        try {
                            ModelObjectInterface m = null;
                            while((m = objectsToSave.pull()) != null){
                                save(m);
                            }
                        } catch (Exception e){
                            log.error("Some error in LazyThread: " + e, e);
                        }
                        try {
                            synchronized (lazyThread){
                                this.wait(1_000);
                            }
                        } catch (Exception e) {
                        }
                    }

                }
            });
            lazyThread.setName("lazyThread");
            lazyThread.start();
        }

        public void add(ModelObjectInterface m){
            try{
                objectsToSave.add(m);
                synchronized (lazyThread){
                    lazyThread.notify();
                }
            } catch (Exception e){
                log.error("Adding to LazyThread: " + e, e);
            }
        }


    }

    private static SaveLaterQueue saveLaterQueue = null;


    public static <T extends ModelObjectInterface> void maybeSaveLazy(T object) {
        if(saveLaterQueue == null){
            saveLaterQueue = new SaveLaterQueue();
        }
        saveLaterQueue.add(object);
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


    public static  <T extends ModelObjectInterface> Object getAttributeValue(T modelObject, String attributeName){
        AttributeContainer attributeContainer = ClassReflector.getAttributeContainer(modelObject.getInterface());
        return attributeContainer.getAttributeValue(modelObject, attributeName);
    }

    public static  <T extends ModelObjectInterface> Object setAttributeValue(T modelObjectToSetOn, String attributeName, Object value){
        AttributeContainer attributeContainer = ClassReflector.getAttributeContainer(modelObjectToSetOn.getInterface());
        return attributeContainer.setAttributeValue(modelObjectToSetOn, attributeName, value);
    }


    public static void delete(Class<? extends ModelObjectInterface> moiClass, String objectID) {
        ModelObjectInterface moi = MQL.selectByID(moiClass, objectID);
        delete(moi);
    }


    public static <M extends ModelObjectInterface> M[] concatArrays(
            M[] array1,
            M[] array2, Class<M> interfaceClass) {

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

        return (M[]) arrayList.toArray((M[]) java.lang.reflect.Array.newInstance(
                interfaceClass,
                arrayList.size()));
    }


    public static <M extends Enum> M[] concatArrays(
            M[] array1,
            M[] array2, Class<M> interfaceClass) {

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

        return (M[]) arrayList.toArray((M[]) java.lang.reflect.Array.newInstance(
                interfaceClass,
                arrayList.size()));
    }


    public static <M extends Enum> M[] substractArrays(
            M[] array1,
            M[] array2, Class<M> interfaceClass) {

        if (array1 == null || array1.length == 0) {
            return array1;
        }

        if (array2 == null || array2.length == 0) {
            return array1;
        }

        List arrayList = new ArrayList(Arrays.asList(array1));

        for (int i = 0; i < array2.length; i++) {
            for(int j = 0; j < arrayList.size(); j++){
                if(arrayList.get(j).equals(array2[i])){
                    arrayList.remove(j);
                    break;
                }
            }

        }


        return (M[]) arrayList.toArray((M[]) java.lang.reflect.Array.newInstance(
                interfaceClass,
                arrayList.size()));
    }



    public static <M extends ModelObjectInterface> M[] removeDoubles(
            M[] array,
            Class interfaceClass) {

        if (array == null || array.length == 0) {
            return array;
        }

        HashMap<String, M> map = new HashMap<>();

        for(int i = 0; i < array.length; i++){
            map.put(array[i].getObjectID(), array[i]);
        }

        Collection<M> values = map.values();
        return (M[]) values.toArray((M[]) java.lang.reflect.Array.newInstance(
                interfaceClass,
                values.size()));
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

    public static <T extends ModelObjectInterface> boolean isModelObjectInArray(
            T[] modelObjects,
            T modelObject) {
        return indexOf(modelObjects, modelObject) != -1;
    }


    public static <T extends ModelObjectInterface> int indexOf(
            T[] modelObjects,
            T modelObject) {
        for (int i = 0; modelObjects != null && i < modelObjects.length; i++) {
            if (modelObjects[i] != null
                    && modelObjects[i].equals(modelObject)) {
                return i;
            }
        }
        return -1;
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
