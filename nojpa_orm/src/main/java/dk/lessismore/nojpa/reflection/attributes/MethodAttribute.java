package dk.lessismore.nojpa.reflection.attributes;

import java.lang.reflect.*;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import dk.lessismore.nojpa.reflection.util.*;
import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import org.apache.commons.lang3.StringUtils;

/**
 * This class can access an attribute in a class with get and set methods.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MOREs
 */
public class MethodAttribute extends Attribute {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MethodAttribute.class);
    /**
     * The set method.
     */
    private Method _setMethod = null;

    /**
     * The get method
     */
    private Method _getMethod = null;

    /**
     * The name of the attribute.
     */
    private String _fieldName = null;

    /**
     * The original class (could be an array).
     */
    private Class _orginalAttributeClass = null;

    public MethodAttribute() {}

    public MethodAttribute(Class parentClass) {
        super(parentClass);
    }

    public void setSetMethod(Method setMethod) {
        _setMethod = setMethod;
    }
    public void setGetMethod(Method getMethod) {
        _getMethod = getMethod;
    }
    public Method getSetMethod() {
        return _setMethod;
    }
    public Method getGetMethod() {
        return _getMethod;
    }
    public boolean setAttributeValuePlain(Object objectToSetOn, Object value) {
        if(_setMethod == null) {
            log.error("Trying to set value without no set method " +getAttributeName()+" "+_getMethod);
            return false;
        }
        Object[] arguments = { value };
        try{
            if(_setMethod.getParameterTypes().length == 2 && _setMethod.getParameterTypes()[1].equals(Locale.class)){
                _setMethod.invoke(objectToSetOn, new Object[]{value, null});
                return true;
            } else {
                _setMethod.invoke(objectToSetOn, arguments);
                return true;
            }
        } catch(IllegalAccessException e) {
            reportInvocationFailure(_setMethod, arguments, e);
        } catch(IllegalArgumentException e) {
           reportInvocationFailure(_setMethod, arguments, e);
        } catch(InvocationTargetException e) {
            reportInvocationFailure(_setMethod, arguments, e);
        }
        return false;
    }
    private void reportInvocationFailure(Method method, Object[] arguments, Exception e) {
        List<String> parameterTypes = Lists.transform(Arrays.asList(method.getParameterTypes()), new Function<Class<?>, String>() {
            public String apply(Class<?> aClass) {
                return aClass.getSimpleName();
            }
        });
        String error = String.format("%s %s(%s) cannot be invoked on %s",
                method.getReturnType().getSimpleName(),
                method.getName(),
                StringUtils.join(parameterTypes, ", "),
                arguments == null ? "null" : StringUtils.join(arguments, ", "));
        log.error(error, e);
    }

    public Annotation[] getDeclaredAnnotations() {
        if(_getMethod != null){
            return _getMethod.getDeclaredAnnotations();
        } else return null;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationsClass) {
        Annotation getAnno = _getMethod.getAnnotation(annotationsClass);
        if(getAnno != null){
            return (T) getAnno;
        } else {
            return _setMethod.getAnnotation(annotationsClass);
        }
    }


//    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
//        if(_getMethod != null){
//            T annotation = _getMethod.getAnnotation(annotationClass);
//            return annotation;
//        } else return null;
//    }

    public Object getAttributeValuePrettyPrint(Object objectToGetFrom){
        Object value = getAttributeValue(objectToGetFrom);
        if(value != null && value instanceof Calendar){
            return ((Calendar) value).getTime();
        }
        return value;
    }


    public Object getAttributeValue(Object objectToGetFrom) {
        try{
            if(objectToGetFrom instanceof ModelObject){
                Object result = _getMethod.invoke(((ModelObject) objectToGetFrom).getProxyObject());
//                log.debug("getAttributeValue:Calling " + _getMethod.getName() + "->" + result);
                return result;
            } else {
                return _getMethod.invoke(objectToGetFrom);
            }
        } catch (Exception e){
            System.out.println("getAttributeValue:ERROR: _fieldName = " + _fieldName);
            System.out.println("getAttributeValue:ERROR: objectToGetFrom = " + objectToGetFrom);
            try{
                System.out.println("getAttributeValue:ERROR: objectToGetFrom.getClass() = " + ((ModelObject) objectToGetFrom).getInterface());
            } catch (Exception ex1){}
            if(objectToGetFrom != null){
                System.out.println("getAttributeValue:ERROR: ((ModelObject) objectToGetFrom).getProxyObject() = " + ((ModelObject) objectToGetFrom).getProxyObject());
            }
            System.out.println("getAttributeValue:ERROR: _orginalAttributeClass = " + _orginalAttributeClass);
            System.out.println("getAttributeValue:ERROR: _getMethod = " + _getMethod);
            System.out.println("getAttributeValue:ERROR: _attributeClass = " + _attributeClass);
            final RuntimeException runtimeException = new RuntimeException("Have you type in the right fieldname in this  " + _getMethod + " ????  Some exception on " + _fieldName + " " + _orginalAttributeClass + e);
            log.error("" + _getMethod.getDeclaringClass() + " ... " + _getMethod.getName() + " ... " + _getMethod.toGenericString());
            log.error("" + runtimeException, e);
            throw runtimeException;
        }
    }
    public Class getOriginalClass() {
        if(_orginalAttributeClass == null) {
            if(_getMethod == null && _setMethod == null)
                throw new RuntimeException("Called getClassType, where no get/setMethod was specified");
            if(_getMethod != null)
                _orginalAttributeClass = _getMethod.getReturnType();
            else if(_setMethod != null)
                _orginalAttributeClass = _setMethod.getParameterTypes()[0];
        }
        return _orginalAttributeClass;
    }
    public String getAttributeName() {
        if(_fieldName == null) {
            if(_getMethod == null && _setMethod == null)
                throw new RuntimeException("Called getFieldName, where no get/setMethod was specified");

            if(_getMethod != null)
                _fieldName = ClassAnalyser.getAttributeNameFromMethod(_getMethod);
            else if(_setMethod != null)
                _fieldName = ClassAnalyser.getAttributeNameFromMethod(_setMethod);
        }
        return _fieldName;
    }

    public boolean isWritable() {
        return _setMethod != null;
    }
    public boolean isReadable() {
        return _getMethod != null;
    }
    public boolean isStatic() {
        if(_getMethod != null)
            return ClassAnalyser.isMethodStatic(_getMethod);
        else if(_setMethod != null)
            return ClassAnalyser.isMethodStatic(_setMethod);
        else
            return false;
    }
}
