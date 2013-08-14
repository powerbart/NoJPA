package dk.lessismore.reusable_v4.reflection.attributeconverters;
/**
 * This class can convert a String or Object to an string and back again.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class StringConverter extends AttributeConverter {

    public Object stringToObject(String str) {
        return str;
    }

    public String objectToString(Object object) {
        return object.toString();
    }
    protected Class getObjectClass() {
        return String.class;
    }
}
