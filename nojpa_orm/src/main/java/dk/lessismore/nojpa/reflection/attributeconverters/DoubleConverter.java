package dk.lessismore.nojpa.reflection.attributeconverters;
/**
 * This class can convert a Double to an string and back again.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class DoubleConverter extends AttributeConverter {

    private Class implementionClass = null;

    public DoubleConverter(Class implementionClass) {
        this.implementionClass = implementionClass;
    }

    public Object stringToObject(String str) {
        try {
            return new Double(str);
        }catch(NumberFormatException e) {
            return null;
        }
    }

    public String objectToString(Object object) {
        return ""+((Double)object).doubleValue();
    }
    protected Class getObjectClass() {
        return implementionClass;
    }
}
