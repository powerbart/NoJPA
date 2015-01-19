package dk.lessismore.nojpa.reflection.attributeconverters;
import java.awt.*;

/**
 * This class can convert a Font to an string and back again.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE
 */
public class FontConverter extends AttributeConverter {

    public Object stringToObject(String str) {
        return Font.decode(str);
    }

    public String objectToString(Object object) {
        Font font = (Font)object;
        return ""+font;
    }
    protected Class getObjectClass() {
        return Font.class;
    }
}
