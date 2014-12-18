package dk.lessismore.nojpa.db.statements;

/**
 * This interface defines the basic functionality of a sql statement.
 * All sql statements should implement this interface.
 * <br>This sql interface should make it easier to create different sql statements.
 * It provides a number helper methods in witch you can define the data of the
 * given statement. When you are finished configurating the statement; forinstance
 * setting the table name of a select statement, you can call makeStatement; and
 * a correct sql statement will be produced with the provided data and the
 * standar sql syntax. This might seem a stupid way to create a sql statement;
 * because it is more easy to write the sql statement by hand; which allso
 * makes it easier to understand what the statement does. But a common problem
 * in this way of writing sql statements is that you allways forgets pings around
 * string; or you forget an condition in the where statement. This takes a lot
 * of time to debug. This is what this is made to solve.
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public interface SQLStatement {

    /**
     * Adds a table name to the sql statement. In most cases only one table
     * name is allowed. If you specify more than one name, the first is used !
     */
    public void addTableName(String tableName);

    /**
     * Call this method when you are finished building the sql statement
     */
    public String makeStatement();
}
