package dk.lessismore.nojpa.db.statements;

import dk.lessismore.nojpa.db.*;

import java.util.Map;

/**
 * This interface defines the functionality of an create sql statement.
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public interface CreateSQLStatement extends SQLStatement {

    /**
     * Attribute property indicating that the attribute must not be null.
     */
    public static final int PROPERTY_NOT_NULL = 0;

    /**
     * Attribute property indicating that the attribute is default null.
     */
    public static final int PROPERTY_NULL = 1;
    public static final String[] propertiesAsString = {"NOT NULL", "NULL"};

    public void addAttribute(String attributeName, DbDataType dataType, int[] properties);

    public void addAttribute(String attributeName, DbDataType dataType);

    public void addPrimaryKey(String attributeName);

    public void setNamesToIndex(Map<String, String> namesToIndex);
}
