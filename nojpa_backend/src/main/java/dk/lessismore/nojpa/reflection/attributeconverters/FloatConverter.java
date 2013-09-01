package dk.lessismore.nojpa.reflection.attributeconverters;
/**
 * This class can convert a Float to an string and back again.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class FloatConverter extends AttributeConverter {

    private Class implementionClass = null;

    public FloatConverter(Class implementionClass) {
        this.implementionClass = implementionClass;
    }

    public Object stringToObject(String str) {
        try {
            return new Float(str);
        }catch(NumberFormatException e) {
            return null;
        }
    }

    public String objectToString(Object object) {
        return ""+((Float)object).floatValue();
    }
    protected Class getObjectClass() {
        return implementionClass;
    }
}
