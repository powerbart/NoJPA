package dk.lessismore.nojpa.db.statements;

import dk.lessismore.nojpa.resources.*;
import dk.lessismore.nojpa.db.statements.mysql.*;

/**
 * This is an factory which can make instances of different sql statements,
 * like forinstance update/insert statements. The produced sql statements will
 * be instances which will make sql statements optimesed for a specific database.
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class SQLStatementFactory {

    private static Resources resources = new PropertyResources("db");

    public SQLStatementFactory() {

    }

    public static InsertSQLStatement getInsertSQLStatement() {
        return new MySqlInsertStatement();
    }

    public static UpdateSQLStatement getUpdateSQLStatement() {
        return new MySqlUpdateStatement();
    }

    public static DeleteSQLStatement getDeleteSQLStatement() {
        return new MySqlDeleteStatement();
    }

    public static SelectSQLStatement getSelectSQLStatement() {
        return new MySqlSelectStatement();
    }

    public static CreateSQLStatement getCreateSQLStatement() {
        return new MySqlCreateStatement();
    }

    public static DropSQLStatement getDropSQLStatement() {
        return new MySqlDropStatement();
    }

    public static LeafExpression getLeafExpression() {
        return new MySqlLeafExpression();
    }

    public static ContainerExpression getContainerExpression() {
        return new MySqlContainerExpression();
    }
}
