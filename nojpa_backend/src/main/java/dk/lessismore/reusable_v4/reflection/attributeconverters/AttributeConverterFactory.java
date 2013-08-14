package dk.lessismore.reusable_v4.reflection.attributeconverters;

import java.util.*;
import java.awt.*;
/**
 * This class is a factory where you can get attribute converters for different
 * kinds of classes. A converter kan parse a string into an instance of the
 * given class it represents and the other way. If you want the reflection
 * framework to be able to convert at given class; you must make a converter
 * class which extends the abstract <tt>AttributeConverter</tt> class and registrate a
 * copy of the class here => <tt>registrateConverter</tt>. By default there will be
 * registreted the most common converters for the primitive types; like
 * int, float, String etc.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class AttributeConverterFactory {

    /**
     * Singelton object.
     */
    private static AttributeConverterFactory attributeConverterFactory = null;

    /**
     * The converters (key=class, value=AttributeConverter).
     */
    private Map converters = new HashMap();

    public AttributeConverterFactory() {
        try {
            registrateConverter(Integer.class, new IntegerConverter(Integer.class));
            registrateConverter(Integer.TYPE, new IntegerConverter(Integer.TYPE));
            registrateConverter(Boolean.class, new BooleanConverter(Boolean.class));
            registrateConverter(Boolean.TYPE, new BooleanConverter(Boolean.TYPE));
            registrateConverter(Double.class, new DoubleConverter(Double.class));
            registrateConverter(Double.TYPE, new DoubleConverter(Double.TYPE));
            registrateConverter(Float.class, new FloatConverter(Float.class));
            registrateConverter(Float.TYPE, new FloatConverter(Float.TYPE));
            registrateConverter(Long.class, new LongConverter(Long.class));
            registrateConverter(Long.TYPE, new LongConverter(Long.TYPE));
            registrateConverter(Short.class, new ShortConverter(Short.class));
            registrateConverter(Short.TYPE, new ShortConverter(Short.TYPE));
            registrateConverter(Character.class, new CharConverter(Character.class));
            registrateConverter(Character.TYPE, new CharConverter(Character.TYPE));
            registrateConverter(Byte.class, new ByteConverter(Byte.class));
            registrateConverter(Byte.TYPE, new ByteConverter(Byte.TYPE));
            registrateConverter(Class.class, new ClassConverter());
            registrateConverter(Font.class, new FontConverter());
            registrateConverter(Color.class, new ColorConverter());
            registrateConverter(String.class, new StringConverter());
            registrateConverter(Calendar.class, new DateConverter());
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a copy of the singelton.
     */
    public static AttributeConverterFactory getInstance() {
        if(attributeConverterFactory == null)
            attributeConverterFactory = new AttributeConverterFactory();
        return attributeConverterFactory;
    }

    /**
     * The converters in a hashmap (key=class, value=AttributeConverter).
     */
    public Map getConverters() {
        return converters;
    }

    /**
     * Do we have a converter for this kind of class.
     */
    public boolean isConvertable(Class targetClass) {
        return getConverters().get(targetClass) != null;
    }

    /**
     * Get an converter which can convert the class. If there is no converter
     * registrated; this will return null.
     */
    public AttributeConverter getConverter(Class targetClass) {
        return (AttributeConverter)getConverters().get(targetClass);
    }

    /**
     * Registrate a new converter at the factory.
     */
    public void registrateConverter(Class destinationClass, AttributeConverter attributeConverter) {
        getConverters().put(destinationClass, attributeConverter);
    }
}
