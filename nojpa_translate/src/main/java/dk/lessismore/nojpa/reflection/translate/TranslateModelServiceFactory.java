package dk.lessismore.nojpa.reflection.translate;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public final class TranslateModelServiceFactory {

    private static HashMap<Class, TranslateModelService> map = new HashMap<>();

    public static <T extends ModelObjectInterface> TranslateModelService<T> getInstance(Class<T> t) {
        return map.get(t);
    }

    public static <T extends ModelObjectInterface> void put(Class<T> t, TranslateModelService<T> translateModelService) {
        map.put(t, translateModelService);
    }




}
