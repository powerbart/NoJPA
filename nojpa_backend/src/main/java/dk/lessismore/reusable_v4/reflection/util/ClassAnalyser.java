package dk.lessismore.reusable_v4.reflection.util;

import org.apache.log4j.Logger;

import java.util.*;
import java.lang.reflect.*;

/**
 * This class is a doggy bag; for all different kind of util methods which is nice
 * to have when you are dealing with reflection.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class ClassAnalyser {

    private transient static final Logger log = Logger.getLogger(ClassAnalyser.class);


    public ClassAnalyser() {}

    /**
     * Find the methods which match the method name.
     * @param methodName The method name to find matches on.
     * @param targetClass The class to find methods in.
     * @return the matching methods.
     */
    public static Vector findMatchingMethods(String methodName, Class targetClass) {
        Vector matchingMethods = new Vector(3);
        Method[] methods = targetClass.getMethods();
        for(int i = 0; i < methods.length; i++) {
            if(methods[i].getName().equalsIgnoreCase(methodName)) {
                matchingMethods.addElement(methods[i]);
            }
        }
        return matchingMethods;
    }

    /**
     * Find a method with the same method name, and which has no arguments.
     */
    public static Method findMatchingMethod(String methodName, Class targetClass) {
        return findMatchingMethod(methodName, targetClass, new Class[] {});
    }

    /**
     * Find a method which matches the method name; and har the given nr of argumenets of the given types.
     */
    public static Method findMatchingMethod(String methodName, Class targetClass, Class[] prefferedArguments) {

        Method matchingMethod = null;
        Method[] methods = targetClass.getMethods();
        for(int i = 0; i < methods.length; i++) {
            if(methods[i].getName().equalsIgnoreCase(methodName)) {
                Class[] argumentTypes = methods[i].getParameterTypes();
                if(argumentTypes.length == prefferedArguments.length) {
                    matchingMethod = methods[i];
                    boolean argumentMatch = false;
                    for(int j = 0; j < prefferedArguments.length; j++) {
                        argumentMatch = argumentTypes[j].isAssignableFrom(prefferedArguments[j]);
                    }
                    if(argumentMatch)
                        return matchingMethod;
                }
            }
        }
        return matchingMethod;
    }

    /**
     * Get the base class of an array.
     */
    public static Class getArrayClass(Object target) {
        return getArrayClass(target.getClass());
    }

    /**
     * Get the base class of an array. (The class type of the elements of the array)
     */
    public static Class getArrayClass(Class targetClass) {
        if(targetClass.isArray()) {
            String className = targetClass.getName();
            int arrayDimension = getArrayDimension(className);
            switch(className.charAt(arrayDimension)) {
                case 'B': return Byte.class;
                case 'C': return Character.class;
                case 'D': return Double.class;
                case 'F': return Float.class;
                case 'I': return Integer.class;
                case 'J': return Long.class;
                case 'S': return Short.class;
                case 'Z': return Boolean.class;
                case 'L':

                    String objectClassName = className.substring(arrayDimension+1, className.length()-1);
                    try {
                        return Class.forName(objectClassName);
                    }catch(Exception e) {}
                    break;
                default:
                    break;
            }
        }
        return null;
    }
    /**
     * Gets the nr of dimensions in the array.
     */
    public static int getArrayDimension(Class targetClass) {
        if(targetClass.isArray()) {
            return getArrayDimension(targetClass.getName());
        }
        else
            return 0;
    }

    /**
     * Gets the nr of dimensions in the array.
     */
    public static int getArrayDimension(String className) {
        int i = 0;
        for(; i < className.length() && className.charAt(i) == '['; i++)
            ;
        return i;
    }

    /**
     * Extracts the attribute name from a get or a set method.
     * The first character of the name will be made into lower case!
     */
    public static String getAttributeNameFromMethod(Method method) {
        String fieldName = method.getName();
        fieldName = fieldName.substring(3, fieldName.length());
        if(fieldName.length() > 0) {
            fieldName = fieldName.substring(0, 1).toLowerCase()+fieldName.substring(1);
        }
        return fieldName;
    }

    /**
     * Determines wether the method is a valid get method.
     */
    public static boolean isValidGetMethod(Method method) {
        String name = method.getName();

        final boolean result = name.startsWith("get") &&
                method.getParameterTypes().length == 0 &&
                !method.getReturnType().getName().equalsIgnoreCase("void") &&
                !method.getName().equalsIgnoreCase("getClass");
        //log.debug("ClassAnalyser.isValidGetMethod("+ method.getDeclaringClass().getSimpleName() +"." + name + ") = " + result);
        return result;
    }

    /**
     * Determines wether the method is a valid set method.
     */
    public static boolean isValidSetMethod(Method method) {
        String name = method.getName();
        if(name.startsWith("set"))
            return method.getParameterTypes().length == 1 && method.getReturnType().getName().equalsIgnoreCase("void");
        else
            return false;

    }

    /**
     * Extracts the attribute name form a field. Any underscore will be removed;
     * and the first letter will be made to lower case.
     */
    public static String getAttributeNameFromField(Field field) {
        String fieldName = field.getName();
        if(fieldName.startsWith("_"))
            fieldName = fieldName.substring(1, fieldName.length());

        return fieldName;

    }

    /**
     * Is this method static.
     */
    public static boolean isMethodStatic(Method method) {
        return !Modifier.isStatic(method.getModifiers());
    }

    /**
     * Is this field static.
     */
    public static boolean isFieldStatic(Field field) {
        return !Modifier.isStatic(field.getModifiers());
    }

    /**
     * Extracts the name of the class from the classpath. If the name is dk.test.Test
     * then the result will be Test.
     */
    public static String getClassName(Class myClass) {
        return myClass.getSimpleName();
    }

    public static Class getImplementationClass(Class someInterface){
        if(someInterface.isInterface()){
            String newFullPackageName = someInterface.getPackage().getName() + ".impl.";
            String className = someInterface.getSimpleName() + "Impl";
            final String newFullClassName = newFullPackageName + className;
            try{

                Class c = Class.forName(newFullClassName);
                return c;
            } catch(Exception e){
                final String message = "Cant find implementation for " + someInterface + " in : " + newFullClassName;
                log.fatal(message);
                throw new RuntimeException(message);
            }
        } else {
            //log.warn(someInterface + " is not an interface.... ");
            return someInterface;
        }

    }



    public static void main(String[] args) {
        ClassAnalyser[][] arg = new ClassAnalyser[4][2];
        System.out.println(ClassAnalyser.getArrayClass(arg.getClass()));
    }

}
