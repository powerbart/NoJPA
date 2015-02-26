package dk.lessismore.nojpa.reflection.db.attributes;

import dk.lessismore.nojpa.reflection.attributes.*;
import dk.lessismore.nojpa.reflection.db.model.*;
import dk.lessismore.nojpa.db.*;

import javax.persistence.Column;
import java.io.*;
import java.lang.annotation.Annotation;

/**
 * This represents an attribute in an database table. It is a wrap around the standard
 * <tt>Attribute</tt> class; but adds a lot of new features; which is only interresting
 * when talking about tables. This is:
 * <ul>
 * <li>A flag indicating if this attribute is a primary key.
 * <li>A flag indicating wether it is a singel association or a multi association (array).
 * <li>A flag indicating wether the multi association is an array of primitives.
 * <li>The data type of the attribute in the database table.
 * </ul>
 * An association is when the attribute is of a class type which is not a database primitive; like
 * String, int or Date. It is an instance of a class which has its own table in the database; and therefor
 * its the attribute accually contains an reference id /association to an tupel in an other table.
 * But why differentiate between singel and multi association ? Well if the attribute has more than
 * one association to another table; which is the case for an array or list; there is not
 * a one to one association. To solve this problem we need an extra association table, where
 * the object which the attribute belongs to is paired with the reference ids of the associated
 * tupels in the associated table.
 * If the multiassociation is an array of primitives and not of modelobjects, the association
 * table; will consist of the primary key of the object; and the different array elements.
 *
 * @author LESS-IS-MORE
 * @version 1.0 21-5-2
 */
public class DbAttribute implements Serializable {

    /**
     * Is this attribute a primary key.
     */
    private boolean primaryKey = false;


    private boolean historyEnableIgnore = false;

    /**
     * The attribute which this Database attribute is based on.
     */
    private Attribute attribute = null;

    private String tableName = null;


    /**
     * The datatype which this attribute is mapped to; in the database.
     */
    private DbDataType dbDataType = null;

    /**
     * Is this attribute a multi association. (an array; of associtions to tupels in an
     * other table.)
     */
    private boolean multiAssociation = false;

    /**
     * Indicates that this attribute is an array of an primitive and should have an
     * association table.
     */
    private boolean primitivArrayAssociation = false;

    /**
     * Is this an association to an tupel in an other table.
     */
    private boolean association = false;

    //private int columnIndex = -1;

    public DbAttribute() {
    }


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    private int nrOfCharacters = 250;
    public void setNrOfCharacters(int nrOfCharacters) {
        this.nrOfCharacters = nrOfCharacters;
    }

    public int getNrOfCharacters() {
        return nrOfCharacters;
    }

//    public int getColumnIndex() {
//        return columnIndex;
//    }
//
//    public void setColumnIndex(int columnIndex) {
//        this.columnIndex = columnIndex;
//    }


    public boolean getHistoryEnableIgnore() {
        return historyEnableIgnore;
    }

    public void setHistoryEnableIgnore(boolean historyEnableIgnore) {
        this.historyEnableIgnore = historyEnableIgnore;
    }

    /**
     * Call this after making a new instance of the class. This method initializes the
     * the instance; and determines wether its an association; the datatype and so one.
     */
    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
        dbDataType = new DbDataType();
        dbDataType.setDbAttribute(this);
        association = ModelObjectInterface.class.isAssignableFrom(attribute.getAttributeClass());
        if (!association) {
            association = attribute.isArray();
            if (association) {
                primitivArrayAssociation = true;
            }
        }

        if (!association) {
            if(attribute.getAttributeClass().equals(String.class)){
                Annotation[] as = attribute.getDeclaredAnnotations();
                if(as != null && as.length > 0){
                  for(int i = 0; i < as.length; i++){
                      if(as[i] instanceof Column){
                        Column c = (Column) as[i];
                        setNrOfCharacters(c.length());
                      }
                  }
                }
            }


            dbDataType.setType(attribute.getAttributeClass());
        } else {
            setNrOfCharacters(32);
            multiAssociation = attribute.isArray();
            if (!multiAssociation) {
                dbDataType.setType(DbDataType.DB_VARCHAR);
            } else if (primitivArrayAssociation) {
                dbDataType.setType(attribute.getAttributeClass());
            }
        }
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public String getAttributeName() {
        return attribute.getAttributeName();
    }

    /**
     * The name of the class which this attribute maps to. The name is not the
     * full classpath; but only the name like <tt>DbAttribute</tt>
     */
    public String getClassName() {
        return attribute.getAttributeClassName();
    }

    public Class getAttributeClass() {
        return attribute.getAttributeClass();
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public DbDataType getDataType() {
        return dbDataType;
    }

    public boolean isAssociation() {
        return association;
    }

    public boolean isMultiAssociation() {
        return multiAssociation;
    }

    public boolean isPrimitivArrayAssociation() {
        return primitivArrayAssociation;
    }

    public String toString() {
        return "DbAtt:" + attribute + " class=" + getClassName() + "\t\tisPrimaryKey=" + isPrimaryKey() + " isAssociation=" + isAssociation() + " isMultiAssociation=" + isMultiAssociation() + " DataType=" + getDataType();
    }

    public String getSolrAttributeName(String prefix){
        String solrAttributeName = (prefix != null && prefix.length() > 0 ? prefix : "") + getTableName() + (getAttributeName().equals("objectID") ? "" : "_" + getAttributeName()) + "__" + (attribute.getSearchFieldAnnotation() != null && attribute.getSearchFieldAnnotation().dynamicSolrPostName() != null && attribute.getSearchFieldAnnotation().dynamicSolrPostName().length() > 1 ? attribute.getSearchFieldAnnotation().dynamicSolrPostName() :  toDefaultSolrType());
        return solrAttributeName;
    }

//    <dynamicField name="*__INT"  type="int"  indexed="true"  stored="true"/>
//    <dynamicField name="*__LONG" type="long"    indexed="true"  stored="true"/>
//    <dynamicField name="*__DOUBLE" type="float"    indexed="true"  stored="true"/>
//    <dynamicField name="*__DATE"  type="date"  indexed="true"  stored="true" />
//    <dynamicField name="*__ID" type="string"  indexed="true"  stored="true"/>
//    <dynamicField name="*__TXT" type="text_general"  indexed="true"  stored="true"/>
//    <dynamicField name="*__BOOL" type="boolean"  indexed="true"  stored="true"/>
//
//    <dynamicField name="*__TXT_ARRAY" type="text_general"  indexed="true"  stored="true" multiValued="true"/>


    public String toDefaultSolrType(){
        if(isMultiAssociation()){
            return "TXT_ARRAY";
        } else {
            switch(dbDataType.getType()) {
                case DbDataType.DB_VARCHAR: return getNrOfCharacters() == 32 || getAttributeClass().isEnum() ? "ID" : "TXT";
                case DbDataType.DB_CHAR: return getNrOfCharacters() == 32 || getAttributeClass().isEnum()  ? "ID" : "TXT";
//                case DbDataType.DB_VARCHAR: return "TXT";
//                case DbDataType.DB_CHAR: return "TXT";
                case DbDataType.DB_INT: return "INT";
                case DbDataType.DB_DOUBLE: return "DOUBLE";
                case DbDataType.DB_DATE: return "DATE";
                case DbDataType.DB_BOOLEAN: return "BOOL";
                case DbDataType.DB_CLOB: return "LONGTEXT";
                case DbDataType.DB_LONG: return "LONG";
                case DbDataType.DB_FLOAT: return "DOUBLE";
//            case DB_CLOB: return "CLOB("+ (dbAttribute != null ? dbAttribute.getNrOfCharacters() : 100000) +")";
                default: return "";
            }
        }
    }


    public boolean isTranslatedAssociation() {
        return attribute.isTranslatedAssociation();
    }
}
