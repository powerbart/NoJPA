package dk.lessismore.nojpa.reflection.maputil;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 19-08-2010
 * Time: 18:17:35
 * To change this template use File | Settings | File Templates.
 */
public class ReusableMapValueConverter {


    public static String convertToString(boolean value) {
        return "" + value;
    }

    public static String convertToString(double value) {
        return "" + value;
    }

    public static String convertToString(float value) {
        return "" + value;
    }

    public static String convertToString(int value) {
        return "" + value;
    }

    public static String convertToString(String value) {
        return value;
    }

    public static String convertToString(String[] value) {
        StringBuilder b = new StringBuilder();
        for(int i = 0; i < value.length; i++){
            b.append(convertToString(value[i]));
            if(i + 1 < value.length){ b.append(", "); };
        }
        return b.toString();
    }


    public static String convertToString(Calendar value) {
        if (value == null) {
            return "NULL";
        }
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(value.getTime());
    }

    public static Object convertFromString(Class attributeClass, String value) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
