package dk.lessismore.nojpa.reflection.attributes;

import java.lang.reflect.*;
import java.lang.annotation.Annotation;
import java.util.Calendar;

import dk.lessismore.nojpa.reflection.util.*;

/**
 * This class can access an attribute in a class by a public field in the class.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE
 */
public class FieldAttribute extends Attribute {

    /**
     * The attribute field
     */
    private Field _field = null;

    /**
     * The name of the attribute.
     */
    private String _fieldName = null;

    public FieldAttribute() {}

    public FieldAttribute(Class parentClass) {
        super(parentClass);
    }

    public void setField(Field field) {
        _field = field;
    }
    public boolean setAttributeValuePlain(Object objectToSetOn, Object value) {
        try{
            _field.set(objectToSetOn, value);
            return true;
        }catch(IllegalAccessException e) { e.printStackTrace(); }
        catch(IllegalArgumentException e) { e.printStackTrace(); }

        return false;
    }

    public Annotation[] getDeclaredAnnotations() {
        return _field.getDeclaredAnnotations();
    }

    public Object getAttributeValuePrettyPrint(Object objectToGetFrom){
        Object value = getAttributeValue(objectToGetFrom);
        if(value != null && value instanceof Calendar){
            return ((Calendar) value).getTime();
        }
        return value;
    }

    public Object getAttributeValue(Object objectToGetFrom) {
        if(_field == null)
            return null;

        try{
            return _field.get(objectToGetFrom);
        }catch(IllegalAccessException e) { }
        catch(IllegalArgumentException e) {}

        return null;
    }
    public Class getOriginalClass() {
        return _field.getType();
    }

    public String getAttributeName() {

        if(_fieldName == null) {
            _fieldName = ClassAnalyser.getAttributeNameFromField(_field);
        }
        return _fieldName;
    }
    public boolean isWritable() {
        return true;
    }
    public boolean isReadable() {
        return true;
    }
    public boolean isStatic() {
        if(_field != null)
            return ClassAnalyser.isFieldStatic(_field);
        return false;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if(_field != null){
            T annotation = _field.getAnnotation(annotationClass);
            return annotation;
        } else return null;
    }

}
