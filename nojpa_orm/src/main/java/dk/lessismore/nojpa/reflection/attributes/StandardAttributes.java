package dk.lessismore.nojpa.reflection.attributes;

import java.util.*;
/**
 * this is a factory which reqistrates all the class types which is considered to
 * be standard or atomic (not dividable.)
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE
 */
public class StandardAttributes {

    /**
     * Singelton.
     */
    public static StandardAttributes standardAttributes = null;

    /**
     * key=class, value=class.
     */
    public static Map standardClasses = new HashMap();

    public StandardAttributes() {

        registrateStandardAttribute(Integer.class);
        registrateStandardAttribute(Boolean.class);
        registrateStandardAttribute(Double.class);
        registrateStandardAttribute(Float.class);
        registrateStandardAttribute(Long.class);
        registrateStandardAttribute(Short.class);
        registrateStandardAttribute(Character.class);
        registrateStandardAttribute(Byte.class);
        registrateStandardAttribute(Class.class);
        registrateStandardAttribute(String.class);
        registrateStandardAttribute(Integer.TYPE);
        registrateStandardAttribute(Boolean.TYPE);
        registrateStandardAttribute(Double.TYPE);
        registrateStandardAttribute(Float.TYPE);
        registrateStandardAttribute(Long.TYPE);
        registrateStandardAttribute(Short.TYPE);
        registrateStandardAttribute(Character.TYPE);
        registrateStandardAttribute(Byte.TYPE);
    }

    /**
     * Call this method if you want an instance of this class. Do not
     * create it yourself.
     */
    public static StandardAttributes getInstance() {
        if(standardAttributes == null)
            standardAttributes = new StandardAttributes();
        return standardAttributes;
    }
    /**
     * Gets the registered standard classes.
     */
    public Map getStandardClasses() {
        return standardClasses;
    }
    /**
     * Is this class a standard class or what.
     */
    public boolean isStandard(Class targetClass) {
        return getStandardClasses().get(targetClass) != null;
    }

    /**
     * Registrate a new standard attribute.
     */
    public void registrateStandardAttribute(Class newStandardAttribute) {
        getStandardClasses().put(newStandardAttribute, newStandardAttribute);
    }

}
