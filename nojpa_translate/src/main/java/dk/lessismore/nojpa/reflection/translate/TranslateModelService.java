package dk.lessismore.nojpa.reflection.translate;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;

import java.lang.reflect.ParameterizedType;
import java.util.List;

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

    public abstract String getSourceLanguage(T object);

    public abstract List<String> getLanguages();

    public abstract String translate(Class modelClass, ModelObjectInterface object, String attributeName, String value, String fromLang, String toLang);

    public abstract T getTranslatedObjectOrNull(T object, String language);

    public abstract void finish(T object, T translatedObjectOrNull, Object translatedDoc, String language);
}
