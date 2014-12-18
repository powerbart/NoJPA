package dk.lessismore.nojpa.reflection.attributeconverters;
/**
 * This class can convert a Short to an string and back again.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE
 */
public class ShortConverter extends AttributeConverter {

    private Class implementionClass = null;

    public ShortConverter(Class implementionClass) {
        this.implementionClass = implementionClass;
    }

    public Object stringToObject(String str) {
        try {
            return new Short(str);
        }catch(NumberFormatException e) {
            return null;
        }
    }

    public String objectToString(Object object) {
        return ""+((Short)object).shortValue();
    }
    protected Class getObjectClass() {
        return implementionClass;
    }
}
