package dk.lessismore.reusable_v4.reflection.db.attributes;

import java.util.*;

public class DbAttributeType {

    public static final int DB_STRING = 0;
    public static final int DB_INT = 1;
    public static final int DB_DOUBLE = 2;
    public static final int DB_BOOLEAN = 3;
    public static final int DB_DATE = 4;
    public static final int DB_FLOAT = 5;

    private int type = DB_STRING;

    public DbAttributeType() {
    }

    public int getType() {
        return type;
    }

    public void setType(Class attributeClass) {
        if (attributeClass.equals(Integer.TYPE)) {
            type = DB_INT;
        } else if (attributeClass.equals(Double.TYPE)) {
            type = DB_DOUBLE;
        } else if (attributeClass.equals(Boolean.TYPE)) {
            type = DB_BOOLEAN;
        } else if (attributeClass.equals(String.class)) {
            type = DB_STRING;
        } else if (attributeClass.equals(Date.class)) {
            type = DB_DATE;
        } else if (attributeClass.equals(Calendar.class)) {
            type = DB_DATE;
        } else if (attributeClass.equals(Float.TYPE)) {
            type = DB_FLOAT;
        }
    }

    public void setType(int type) {
        this.type = type;
    }

    public String toString() {

        switch (type) {
            case DB_STRING:
                return "VARCHAR(200)";
            case DB_INT:
                return "INT";
            case DB_DOUBLE:
                return "DOUBLE";
            case DB_BOOLEAN:
                return "BOOLEAN";
            case DB_DATE:
                return "DATE";
            case DB_FLOAT:
                return "FLOAT";
            default:
                return "";
        }
    }

}
