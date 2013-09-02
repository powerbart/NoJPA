package dk.lessismore.nojpa.reflection.attributeconverters;
/**
 * This class can convert a Long to an string and back again.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class LongConverter extends AttributeConverter {

    private Class implementionClass = null;

    public LongConverter(Class implementionClass) {
        this.implementionClass = implementionClass;
    }

    public Object stringToObject(String str) {
        try {
            return new Long(str);
        }catch(NumberFormatException e) {
            return null;
        }
    }

    public String objectToString(Object object) {
        return ""+((Long)object).longValue();
    }
    protected Class getObjectClass() {
        return implementionClass;
    }
}
