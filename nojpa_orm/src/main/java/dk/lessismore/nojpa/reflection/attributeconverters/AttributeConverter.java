package dk.lessismore.nojpa.reflection.attributeconverters;

import java.util.*;
import java.lang.reflect.*;

/**
 * You should extend this class if you want to make a new converter that can convert
 * a generic class to string and vise versa. You should implement the abstract methods;
 * and if you want to; you can allso override; the other methods in this class; which
 * converts arrays of the given class type.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE
 */
public abstract class AttributeConverter {

    public static String arraySeparator = ",";

    /**
     * Converts a string to an object of the class of interrest.
     */
    public abstract Object stringToObject(String str) ;

    /**
     * Converts an object of the specified class to an string.
     */
    public abstract String objectToString(Object object) ;

    /**
     * Gets the target class of this converter.
     */
    protected abstract Class getObjectClass() ;

    /**
     * Can convert an commaseparated list of string to an array of the given class type.
     */
    public Object[] stringToArray(String arrayStr) {

        StringTokenizer arrayTokens = new StringTokenizer(arrayStr, arraySeparator);
        Object[] array = (Object[])Array.newInstance(getObjectClass(), arrayTokens.countTokens());
        for(int i = 0; arrayTokens.hasMoreTokens(); i++)
            array[i] = stringToObject(arrayTokens.nextToken().trim());

        return array;
    }

    /**
     * Can convert an array of the given class type to an commaseparated string.
     */
    public String arrayToString(Object[] array) {

        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < array.length; i++) {
            if(i > 0 ) {
                buffer.append(arraySeparator);
            }
            buffer.append(objectToString(array[i]));
        }
        return buffer.toString();
    }
}
