package dk.lessismore.reusable_v4.reflection.attributeconverters;
/**
 * This class can convert a Char to an string and back again.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class CharConverter extends AttributeConverter {

    private Class implementionClass = null;

    public CharConverter(Class implementionClass) {
        this.implementionClass = implementionClass;
    }

    public Object stringToObject(String str) {
            return new Character(!str.isEmpty() ? str.charAt(0) : ' ');
    }

    public String objectToString(Object object) {
        return ""+((Character)object).charValue();
    }
    protected Class getObjectClass() {
        return implementionClass;
    }
}
