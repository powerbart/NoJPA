package dk.lessismore.nojpa.db.statements;

import java.sql.*;

/**
 * This interface defines the functionality of an select sql statement.
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public interface PreparedSQLStatement {

    public void addLeafExpression(LeafExpression expression);

    public void makeStatementReadyToExcute(PreparedStatement statement) throws SQLException;

}
