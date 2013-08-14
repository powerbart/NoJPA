package dk.lessismore.reusable_v4.reflection.attributeconverters;

import java.text.*;
import java.util.*;
/**
 * This can convert at Calendar object to the string format dd-MM-yyyy HH:mm:ss
 * and vise versa.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class DateConverter extends AttributeConverter {

    ParsePosition pos = new ParsePosition(0);

    public Object stringToObject(String str) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime((new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).parse(str, pos));
            return calendar;
        }catch(Exception e) {
            return null;
        }
    }

    public String objectToString(Object object) {
        return ""+(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).format(((Calendar)object).getTime());
    }
    protected Class getObjectClass() {
        return Calendar.class;
    }
}
