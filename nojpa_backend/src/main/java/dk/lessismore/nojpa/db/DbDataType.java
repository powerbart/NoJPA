package dk.lessismore.nojpa.db;

import dk.lessismore.nojpa.reflection.db.attributes.DbAttribute;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttributeContainer;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.DbClassReflector;

import javax.persistence.Column;
import java.util.*;
import java.io.*;
import java.lang.annotation.Annotation;

/**
 * This class represents a data type in the database. This could be a string,
 * integer, double, date etc.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class DbDataType implements Serializable {

    public static final int DB_VARCHAR = 0;
    public static final int DB_CHAR = 1;
    public static final int DB_INT = 2;
    public static final int DB_DOUBLE = 3;
    public static final int DB_BOOLEAN = 4;
    public static final int DB_DATE = 5;
    public static final int DB_CLOB = 6;
    public static final int DB_LONG = 7;


    public static final int DB_MAX_VARCHAR_LENGTH = 25000;



    private DbAttribute dbAttribute = null;
    private int type = DB_VARCHAR;

    public DbDataType() {}

    public DbDataType(int type) {
        this.type = type;
    }
    public DbDataType(Class attributeClass) {
        setType(attributeClass);
    }

    public void setDbAttribute(DbAttribute dbAttribute) {
        this.dbAttribute = dbAttribute;
    }

    //    public DbDataType(int type, int nrOfCharacters) {
//        this.type = type;
//        this.nrOfCharacters = nrOfCharacters;
//    }

    public DbDataType(DbAttribute dbAttribute) {
        if(dbAttribute == null) {
            throw new NullPointerException();
        }
        this.dbAttribute = dbAttribute;

        if(ModelObjectInterface.class.isAssignableFrom(dbAttribute.getAttribute().getAttributeClass())){
            DbAttributeContainer attributeContainer = DbClassReflector.getDbAttributeContainer(dbAttribute.getAttribute().getAttributeClass());
            Annotation[] as = attributeContainer.getPrimaryKeyAttribute().getAttribute().getDeclaredAnnotations();
            if(as != null && as.length > 0){
              for(int i = 0; i < as.length; i++){
                  if(as[i] instanceof Column){
                    Column c = (Column) as[i];
                    dbAttribute.setNrOfCharacters(c.length());
                    setType(DB_CHAR);  
                  }
              }
            }
        } else {
            Annotation[] as = dbAttribute.getAttribute().getDeclaredAnnotations();
            this.dbAttribute = dbAttribute;
            if(as != null && as.length > 0){
              for(int i = 0; i < as.length; i++){
                  if(as[i] instanceof Column){
                    Column c = (Column) as[i];
                    dbAttribute.setNrOfCharacters(c.length());
                  }
              }
            }
            setType(dbAttribute.getAttributeClass());
        }
    }



    public int getType() {
        return type;
    }
    /**
     * This method will analyse the class; and finde the data type which suits the
     * class.
     */
    public void setType(Class attributeClass) {
        if(attributeClass.equals(Integer.TYPE))
            type = DB_INT;
        else if(attributeClass.equals(Long.TYPE))
            type = DB_LONG;
        else if(attributeClass.equals(Double.TYPE))
            type = DB_DOUBLE;
        else if(attributeClass.equals(Boolean.TYPE))
            type = DB_BOOLEAN;
        else if(attributeClass.equals(String.class))
            type = DB_VARCHAR;
        else if(attributeClass.equals(Calendar.class))
            type = DB_DATE;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String toString() {

        switch(type) {
            case DB_VARCHAR:
                if(dbAttribute != null){
                    Annotation[] as = dbAttribute.getAttribute().getDeclaredAnnotations();
                    if(as != null && as.length > 0){
                      for(int i = 0; i < as.length; i++){
                          if(as[i] instanceof Column){
                            Column c = (Column) as[i];
                            if(c.length() < 250){
                                setType(DB_CHAR);
                                dbAttribute.setNrOfCharacters(c.length());
                                return toString();

                            } else if(c.length() > DB_MAX_VARCHAR_LENGTH) {
                                setType(DB_CLOB);
                                return toString();
                            } else {
                                dbAttribute.setNrOfCharacters(c.length());
                            }
                          }
                      }
                    }
                }
                return "VARCHAR("+ (dbAttribute != null ? dbAttribute.getNrOfCharacters() : 250)+")";
            case DB_CHAR: return "CHAR("+(dbAttribute != null ? dbAttribute.getNrOfCharacters() : 32)+")";
            case DB_INT: return "INT";
            case DB_DOUBLE: return "DOUBLE PRECISION";
            case DB_DATE: return "DATETIME";
            case DB_BOOLEAN: return "INT";
            case DB_LONG: return "BIGINT(64)";
            case DB_CLOB: return "LONGTEXT";
//            case DB_CLOB: return "CLOB("+ (dbAttribute != null ? dbAttribute.getNrOfCharacters() : 100000) +")";
            default: return "";
        }
    }





}
