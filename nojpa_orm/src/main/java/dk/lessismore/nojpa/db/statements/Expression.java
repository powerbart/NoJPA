package dk.lessismore.nojpa.db.statements;

/**
 * This interface represents an expression in the where part of an select
 * or delete statement. An expression can contain a number of
 * comparisions and joins, with paranteses and conditions (and/or).
 * <br><tt>Forinstance: ((a = b and b = 2) or (a = c and c = 3))</tt>
 * <br>This expression contains 2 expression which again contains
 * 2 smaller expressions ! Therefore an expression can either contain
 * other expressions <tt>(ContainerExpression)</yy >or one one expression
 * <tt>(LeafExpression)</tt> like <tt>a = b</tt>.
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public interface Expression {

    /**
     * This call will make the expression generate an string notation
     * of the configured expression.
     */
    public String makePreparedStatement(PreparedSQLStatement preparedSQLStatement);

    public String makeStatement();

//    public int getLastCondition();
//
//    public void setLastCondition(int lastCondition);


}
