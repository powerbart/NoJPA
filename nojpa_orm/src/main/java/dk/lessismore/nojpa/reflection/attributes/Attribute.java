package dk.lessismore.nojpa.reflection.attributes;

import dk.lessismore.nojpa.reflection.db.annotations.DbStrip;
import dk.lessismore.nojpa.reflection.db.annotations.SearchField;
import dk.lessismore.nojpa.reflection.visitors.*;
import dk.lessismore.nojpa.reflection.*;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.util.*;
import dk.lessismore.nojpa.reflection.attributeconverters.*;
import java.util.*;
import java.lang.annotation.Annotation;

/**
 * This class represents an reflected attribute in a class. It is abstract because the attribute can
 * be based on either an method access or an field access.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE
 */
public abstract class Attribute {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Attribute.class);
    protected boolean translatedAssociation = false;
    protected boolean unique = false;
    private SearchField searchFieldAnnotation;
    private DbStrip dbStripAnnotation;

    public Attribute() {}

    public Attribute(Class parentClass) {
        _parentClass = parentClass;
    }

    /**
     * The target class of the attribute container which holdes this attribute.
     */
    private Class _parentClass = null;
    private Class _declaringClass = null;
    /**
     * The attribute class.
     */
    protected Class _attributeClass = null;


    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    /**
     * Sets the attribute value right on. No evaluation of anykind.
     */
    protected abstract boolean setAttributeValuePlain(Object objectToSetOn, Object value) ;


    public abstract Annotation[] getDeclaredAnnotations();

    /**
     * Gets the attribute value from the given object-
     */
    public abstract Object getAttributeValue(Object objectToGetFrom);

    public abstract Object getAttributeValuePrettyPrint(Object objectToGetFrom);

    /**
     * Gets the name of the attribute.
     */
    public abstract String getAttributeName();

    /**
     * Is this attribute writable. (this is the case if only a get method is present)
     */
    public abstract boolean isWritable();

    /**
     * Is this attribute readable (this is the case if only a set method is present)
     */
    public abstract boolean isReadable();

    /**
     * Is this a static field this attribute represents.
     */
    public abstract boolean isStatic();

    /**
     * Gets the original class. This is only of interrest if this class is an array. Then there is
     * a difference between the class and the original class.
     */
    public abstract Class getOriginalClass() ;

    /**
     * Can we convert this attribute from a string to a object.
     */
    public boolean isConvertable() {
        return AttributeConverterFactory.getInstance().getConverter(getAttributeClass()) != null;
    }

    public boolean isModelObject(){
        return ModelObjectInterface.class.isAssignableFrom(getAttributeClass());
    }

    /**
     * Is this a standard attribute. A standard attribute is the normal types like int, String
     * etc.
     */
    public boolean isStandard() {
        return StandardAttributes.getInstance().isStandard(getAttributeClass());
    }

    public boolean isArray() {
        return getOriginalClass().isArray();
    }

    /**
     * Gets the name of the Class which this attribute represents. Like forinstance "Attribute";
     * the classpath is ignored.
     */
    public String getAttributeClassName() {
        return ClassAnalyser.getClassName(getAttributeClass());
    }

    /**
     * Sets the attribute value on the given object.
     * @return If the value has been set successfull <tt>true</tt> is returned.
     */
    public boolean setAttributeValue(Object objectToSetOn, Object value) {

        if(isWritable()) {

            //Can this value be assigned to the attribute.
            //Is Attribute; super class of value class =>
            if(value != null && getOriginalClass().isAssignableFrom(value.getClass())) {

                return setAttributeValuePlain(objectToSetOn, value);
            }

            //Its an string. we must convert it to the attribute class instance.
            else if(value instanceof String) {
                AttributeConverter converter = AttributeConverterFactory.getInstance().getConverter(getAttributeClass());
                if(converter != null) {
                    if(!isArray())
                        value = converter.stringToObject((String)value);
                    else
                        value = converter.stringToArray((String)value);
                    if(value != null)
                        return setAttributeValuePlain(objectToSetOn, value);
                }
            }
            else {
                return setAttributeValuePlain(objectToSetOn, value);
            }
        }

        return false;
    }

    /**
     * gets the class type of this attribute. If the attribute is an array; the class type of
     * the array elements is returned.
     */
    public final Class getAttributeClass() {
        if(_attributeClass == null) {
            _attributeClass = getOriginalClass();
            if(_attributeClass.isArray())
                _attributeClass = ClassAnalyser.getArrayClass(_attributeClass);
        }
        return _attributeClass;
    }

    public Class getDeclaringClass() {
        return _declaringClass;
    }

    public void setDeclaringClass(Class _declaringClass) {
        this._declaringClass = _declaringClass;
    }

    /**
     * Visit method.
     */
    public void visit(AttributeContainerVisitor visitor, String prefix) {
        Class attributeClass = getAttributeClass();
        if(!StandardAttributes.getInstance().isStandard(attributeClass)) {
            AttributeContainer container = ClassReflector.getAttributeContainer(attributeClass);
            if(container != null) {
                visitor.visitContainer(container,makePrefix(prefix, getAttributeName()));
            }
        }
    }

    /**
     * Visit method.
     */
    public void visit(AttributeVisitor visitor, String prefix) {

        Class attributeClass = getAttributeClass();

        //log.debug(attributeClass.getName());
        if(!StandardAttributes.getInstance().isStandard(attributeClass)) {
            AttributeContainer container = ClassReflector.getAttributeContainer(attributeClass);
            if(container != null) {
                container.visit(visitor, prefix);
            }
        }
    }
    /**
     * adds the attribute name to the prefix string.
     */
    public static String makePrefix(String prefix, String attributeName) {
        if(prefix.isEmpty())
            prefix = attributeName;
        else
            prefix = prefix+"."+attributeName;
        return prefix;
    }
    public String toString() {
        return "Att: name("+getAttributeName() + ") attribute.type(" + getAttributeClassName()+") isArray("+isArray()+") isWriteable("+isWritable()+") isReadable("+isReadable() +")";
    }

    public abstract <T extends Annotation> T getAnnotation(Class<T> annotationsClass);

    public void visitValue(ModelObjectInterface instance, DbValueVisitor visitor) {
        Class attributeClass = getAttributeClass();
        Object value = getAttributeValue(instance);
        if (isArray()) visitor.visit((ModelObjectInterface[]) value, attributeClass.getComponentType());
        else if (attributeClass.equals(Integer.class)) visitor.visit((Integer) value);
        else if (attributeClass.equals(int.class)) visitor.visit((int) (Integer)value);

        else if (attributeClass.equals(Float.class)) visitor.visit((Float) value);
        else if (attributeClass.equals(float.class)) visitor.visit((float) (Float)value);

        else if (attributeClass.equals(Double.class)) visitor.visit((Double) value);
        else if (attributeClass.equals(double.class)) visitor.visit((double) (Double)value);

        else if (attributeClass.equals(Boolean.class)) visitor.visit((Boolean) value);
        else if (attributeClass.equals(boolean.class)) visitor.visit((boolean) (Boolean)value);

        else if (attributeClass.equals(String.class)) visitor.visit((String) value);
        else if (Calendar.class.isAssignableFrom(attributeClass)) visitor.visit((Calendar) value);
        else if (ModelObjectInterface.class.isAssignableFrom(attributeClass)) visitor.visit((ModelObjectInterface) value, attributeClass);
        else throw new RuntimeException("Unknown DB type: "+attributeClass.getName());
    }


    public boolean isTranslatedAssociation() {
        return translatedAssociation;
    }

    public void setTranslatedAssociation(boolean translatedAssociation) {
        this.translatedAssociation = translatedAssociation;
    }

    public void setSearchFieldAnnotation(SearchField searchFieldAnnotation) {
        this.searchFieldAnnotation = searchFieldAnnotation;
    }

    public SearchField getSearchFieldAnnotation() {
        return searchFieldAnnotation;
    }

    public DbStrip getDbStripAnnotation() {
        return dbStripAnnotation;
    }

    public void setDbStripAnnotation(DbStrip dbStripAnnotation) {
        this.dbStripAnnotation = dbStripAnnotation;
    }
}
