package dk.lessismore.nojpa.db.statements.oracle;

import dk.lessismore.nojpa.db.statements.DropSQLStatement;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class OracleDropStatement extends OracleStatement implements DropSQLStatement {

    public String makeStatement() {

        if (getTableNames().isEmpty()) {
            throw new RuntimeException("Carnt make drop statement without tablename");
        }
        return "drop table if exists " + getTableNames().get(0);
    }

}
