package dk.lessismore.nojpa.rest;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.format.Formatter;

import java.lang.annotation.Annotation;
import java.text.ParseException;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: niakoi
 * Date: 3/24/14
 * Time: 11:41 PM
 */
public class NoJpaFormatter implements Formatter<ModelObjectInterface> {

    private Class clazz;

    public NoJpaFormatter(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public ModelObjectInterface parse(String text, Locale locale) throws ParseException {
        Locator locator = AnnotationUtils.getAnnotation(clazz, Locator.class);
        if (locator != null) {
            try {
                return locator.locator().newInstance().get(text);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return MQL.selectByID(clazz, text);
    }

    @Override
    public String print(ModelObjectInterface object, Locale locale) {
        Printer printer = AnnotationUtils.getAnnotation(clazz, Printer.class);
        if (printer != null) {
            try {
                return printer.printer().newInstance().put(object);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return object.getObjectID();
    }

    public Class getClazz() {
        return clazz;
    }
}