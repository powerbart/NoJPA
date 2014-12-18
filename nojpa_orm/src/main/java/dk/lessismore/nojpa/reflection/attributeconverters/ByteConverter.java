package dk.lessismore.nojpa.reflection.attributeconverters;
/**
 * This class can convert an Byte to an string and back again.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE
 */
public class ByteConverter extends AttributeConverter {

    private Class implementionClass = null;

    public ByteConverter(Class implementionClass) {
        this.implementionClass = implementionClass;
    }

    public Object stringToObject(String str) {
            return new Byte(str);
    }

    public String objectToString(Object object) {
        return ""+((Byte)object).byteValue();
    }
    protected Class getObjectClass() {
        return implementionClass;
    }
}
