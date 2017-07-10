package dk.lessismore.nojpa.db.statements.mysql;

import dk.lessismore.nojpa.db.SQLStatementExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.text.*;

/**
 * This class helps to convert the different data types to the correct string
 * format in the mysql statements.
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public class MySqlUtil {

    private static Logger log = LoggerFactory.getLogger(MySqlUtil.class);

    public static String convertToSql(Object value, Class type) {
        if(type.equals(Integer.class)){
            return MySqlUtil.convertToSql((Integer) value);
        } else if(type.equals(Double.class)){
            return MySqlUtil.convertToSql((Double) value);
        } else if(type.equals(Float.class)){
            return MySqlUtil.convertToSql((Float) value);
        } else if(type.equals(Long.class)){
            return MySqlUtil.convertToSql((Long) value);
        } else if(type.equals(Boolean.class)){
            return MySqlUtil.convertToSql((Boolean) value);
        } else if(type.equals(Calendar.class)){
            return MySqlUtil.convertToSql((Calendar) value);
        } else if(type.equals(String.class)){
            return MySqlUtil.convertToSql((String) value);
        } else {
            throw new RuntimeException("Don't know what to do with type("+ type +")->("+ value +") ");
        }
    }
    
    
    
    public static String convertToSql(boolean value) {
        return "" + value;
    }

    public static String convertToSql(double value) {
        return "" + value;
    }

    public static String convertToSql(float value) {
        return "" + value;
    }

    public static String convertToSql(long value) {
        return "" + value;
    }

    public static String convertToSql(int value) {
        return "" + value;
    }

    public static String convertToSql(String value) {
        // TODO: Escape this value. JDBC seems to provide no means to escape strings... though it's hard to believe.
        // Possible SQL injection string attacks for different escape techniques
        // Replace ' with '', try this input (I haven't):
        // \'; drop database FooBar --                      after escaping and quoting:
        // '\''; drop database FooBar --'                   will the backslash escape one of the ticks?
        // Replace \ with \\... are you sure that you've covered all cases?
        // Is there any way to exploit unicode to bypass the escaping? Known bugs in the JDBC/MySQL driver?
        // I'm not sure how to escape a string, so I won't create an escaping function that may only provide
        // a false sense of security. I can't think of any other way than disabling dynamic queries altogether.
        // Maybe it doesn't matter since they're not used in release mode. (- ahnfelt)
        // from Atanas: ESAPI seems a best-practice security framework, if we don't go with prep statements - this is the way to do it
        if(value == null || value.equals("null") || value.equals("Null")) return "NULL";
//        return '\'' + ESAPI.encoder().encodeForSQL(new MySQLCodec(MySQLCodec.Mode.STANDARD), value) + '\'';
        return '\'' + value.replaceAll("'", "''") + '\'';
    }

    public static String convertToSql(String[] value) {
            // TODO: Escape this value. JDBC seems to provide no means to escape strings... though it's hard to believe.
            // Possible SQL injection string attacks for different escape techniques
            // Replace ' with '', try this input (I haven't):
            // \'; drop database FooBar --                      after escaping and quoting:
            // '\''; drop database FooBar --'                   will the backslash escape one of the ticks?
            // Replace \ with \\... are you sure that you've covered all cases?
            // Is there any way to exploit unicode to bypass the escaping? Known bugs in the JDBC/MySQL driver?
            // I'm not sure how to escape a string, so I won't create an escaping function that may only provide
            // a false sense of security. I can't think of any other way than disabling dynamic queries altogether.
            // Maybe it doesn't matter since they're not used in release mode. (- ahnfelt)
        StringBuilder b = new StringBuilder();
        b.append("(");
        for(int i = 0; i < value.length; i++){
            b.append(convertToSql(value[i]));
            if(i + 1 < value.length){ b.append(", "); };
        }
        b.append(")");
            return b.toString();
        }


    public static String convertToSql(Calendar value) {
        if (value == null) {
            return "NULL";
        }
        return '\'' + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(value.getTime()) + '\'';
    }

    public static String convertToPreparedSql(boolean value) {
        return "" + value;
    }

    public static String convertToPreparedSql(double value) {
        return "" + value;
    }

    public static String convertToPreparedSql(float value) {
        return "" + value;
    }

    public static String convertToPreparedSql(int value) {
        return "" + value;
    }

    public static String convertToPreparedSql(String value) {
        return value;
    }

    public static String convertToPreparedSql(String[] values) {
        StringBuilder b = new StringBuilder();
        b.append("(");
        for(int i = 0; i < values.length; i++){
            b.append(convertToSql(values[i]));
            if(i + 1 < values.length){ b.append(", "); };
        }
        b.append(")");
        return b.toString();
    }

    public static String convertToPreparedSql(Calendar value) {
        if (value == null) {
            return "NULL";
        }
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(value.getTime());
    }
}
