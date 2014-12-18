package dk.lessismore.nojpa.db.statements;

import java.util.*;

/**
 * This interface represents an singel expression. This could be
 * a=2... or forinstance a.primaryKey = b.primarykey.
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public interface LeafExpression extends Expression {

    public LeafExpression addJoin(String attributeName1, String attributeName2);

    public LeafExpression addJoin(String tableName1, String attributeName1, String tableName2, String attributeName2);

    public LeafExpression addConstrain(String attributeName, int comparator, String value);

//    public LeafExpression whereIn(String attributeName, String[] values);

    public LeafExpression addConstrain(String attributeName, int comparator, Calendar value);

    public LeafExpression addConstrain(String attributeName, int comparator, int value);

    public LeafExpression addConstrain(String attributeName, int comparator, double value);

    public LeafExpression addConstrain(String attributeName, int comparator, float value);

    public LeafExpression addConstrain(String attributeName, int comparator, long value);

    public LeafExpression addConstrain(Class sourceClass, String attributeName, int comparator, String value);

    public LeafExpression addConstrain(Class sourceClass, String attributeName, int comparator, Calendar value);

    public LeafExpression addConstrain(Class sourceClass, String attributeName, int comparator, int value);

    public LeafExpression addConstrain(Class sourceClass, String attributeName, int comparator, double value);

    public LeafExpression addConstrain(Class sourceClass, String attributeName, int comparator, float value);

    public LeafExpression isNull(String attributeName);

    public LeafExpression isNotNull(String attributeName);

    public LeafExpression setToFalse();

    public String getPreparedValue();

}
