package dk.lessismore.reusable_v4.reflection.attributeconverters;
/**
 * This class can convert an Boolean to an string and back again.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class BooleanConverter extends AttributeConverter {

    public static String trueString = "true";
    public static String falseString = "false";
    private Class implementionClass = null;

    public BooleanConverter(Class implementionClass) {
        this.implementionClass = implementionClass;
    }
    public Object stringToObject(String str) {
        return new Boolean(str.equalsIgnoreCase("true") || str.equalsIgnoreCase("on") || str.equals("1") || str.equals("yes"));
    }

    public String objectToString(Object object) {
        if(((Boolean)object).booleanValue())
            return trueString;
        else
            return falseString;
    }
    protected Class getObjectClass() {
        return implementionClass;
    }
}
