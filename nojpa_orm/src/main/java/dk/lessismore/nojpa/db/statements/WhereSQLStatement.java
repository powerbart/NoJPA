package dk.lessismore.nojpa.db.statements;

import java.util.*;

/**
 * This interface defines the functionality of the where part of an
 * select or delete sql statement.
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public interface WhereSQLStatement {

    public static final int EQUAL = 0;
    public static final int EQUAL_OR_GREATER = 1;
    public static final int EQUAL_OR_LESS = 2;
    public static final int GREATER = 3;
    public static final int LESS = 4;
    public static final int NOT_EQUAL = 5;
    public static final int LIKE = 6;
    public static final int NOT_LIKE = 7;
    public static final String[] comparatorAsStr = {
            "=",
            ">=",
            "<=",
            ">",
            "<",
            "!=",
            "LIKE",
            "NOT LIKE"
    };

    /**
     * And condition
     */
    public static final int AND = 0;

    /**
     * Or condition.
     */
    public static final int OR = 1;
    public static final String[] conditionAsStr = {
            "and",
            "or"
    };

    /**
     * Adds a join expression to the where statement. Remember to add the
     * table name ;)
     */
    public void addJoin(String attributeName1, String attributeName2);

    public void addJoin(String tableName1, String attributeName1, String tableName2, String attributeName2);

    /**
     * A constrain is an expression which compares a attribute with a value.
     * Like forinstance <tt>a = 10</tt>.
     */
    public void addConstrain(String attributeName, int comparator, String value);

//    public void whereIn(String attributeName, String[] values);

    public void addConstrain(String attributeName, int comparator, Calendar value);

    public void addConstrain(String attributeName, int comparator, int value);

    public void addConstrain(String attributeName, int comparator, double value);

    public void addConstrain(String attributeName, int comparator, float value);

    public void isNull(String attributeName);

    public void isNotNull(String attributeName);

    public void addExpression(Expression expression);

    public void addExpression(int condition, Expression expression);
}
