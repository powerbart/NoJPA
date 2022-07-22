package dk.lessismore.nojpa.reflection.translate;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import java.lang.reflect.ParameterizedType;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public abstract class TranslateModelService<T extends ModelObjectInterface> {

    protected Class<T> modelObjectClass;

    @SuppressWarnings("unchecked")
    protected TranslateModelService() {
        this.modelObjectClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public abstract T translate(T obj, String from, String to) throws Exception;

    protected T create() {
        // TODO seb create a pojo of modelObjectClass?
        return null;
    }
}
