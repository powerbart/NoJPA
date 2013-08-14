package dk.lessismore.reusable_v4.db.statements;

import java.util.*;

/**
 * This interface represents an expression which can contain a number of
 * expressions with conditions between them. This could be forinstance
 * (!exp1! and !exp2! and !exp2!). The expressions can either be Container
 * expressions or Leaf expressions.
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public interface ContainerExpression extends Expression {

    /**
     * Adds an expression.
     */
    public ContainerExpression addExpression(Expression expression);

    /**
     * @param condition The condition to add this expression to the previous.
     *                  The condition can either be AND/OR. Condition is defined in
     *                  WhereSqlStatement.
     */
    public ContainerExpression addExpression(int condition, Expression expression);

    /**
     * @param condition The condition to add this expression to the previous.
     *                  The condition can either be AND/OR. Condition is defined in
     *                  WhereSqlStatement.
     */
    public ContainerExpression addCondition(int condition);
//    public ContainerExpression reAddCondition(int condition);

    public int nrOfExpressions();


}
