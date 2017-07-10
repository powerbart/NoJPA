package dk.lessismore.nojpa.db.statements;

import dk.lessismore.nojpa.utils.Pair;

import java.util.*;

/**
 * This interface defines the functionality of an insert sql statement.
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public interface InsertSQLStatement extends SQLStatement {

    Map<String, Pair<Object, Class>> getAttributeValuesAndTypes();

    public void addAttributeValue(String attributeName, int value);

    public void addAttributeValue(String attributeName, double value);

    public void addAttributeValue(String attributeName, float value);

    public void addAttributeValue(String attributeName, long value);

    public void addAttributeValue(String attributeName, boolean value);

    public void addAttributeValue(String attributeName, Calendar value);

    public void addAttributeValue(String attributeName, String value);

    String makePreparedStatement(PreparedSQLStatement preSQLStatement);
}
