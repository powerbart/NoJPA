package dk.lessismore.nojpa.reflection.attributeconverters;
/**
 * This class can convert a Integer to an string and back again.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE
 */
public class IntegerConverter extends AttributeConverter {

    private Class implementionClass = null;

    public IntegerConverter(Class implementionClass) {
        this.implementionClass = implementionClass;
    }

    public Object stringToObject(String str) {
        try {
            return new Integer(str);
        }catch(NumberFormatException e) {
            return null;
        }
    }

    public String objectToString(Object object) {
        return ""+((Integer)object).intValue();
    }
    protected Class getObjectClass() {
        return implementionClass;
    }
}
