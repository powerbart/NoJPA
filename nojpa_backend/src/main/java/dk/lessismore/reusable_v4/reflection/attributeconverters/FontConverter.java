package dk.lessismore.reusable_v4.reflection.attributeconverters;
import java.awt.*;
import java.util.*;
/**
 * This class can convert a Font to an string and back again.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
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
