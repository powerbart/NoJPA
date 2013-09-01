package dk.lessismore.nojpa.db.statements.mysql;

import dk.lessismore.nojpa.db.statements.*;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class MySqlDeleteStatement extends MySqlWhereStatement implements DeleteSQLStatement {

    public String makeStatement() {
        if (getTableNames().isEmpty()) {
            throw new RuntimeException("Carnt make insert statement without tablename");
        }

        StringBuilder statement = new StringBuilder();
        statement.append("delete from " + getTableNames().get(0));
        statement.append("\n" + super.makeStatement());

        return statement.toString();
    }

    public static void main(String[] args) {
        MySqlDeleteStatement s = new MySqlDeleteStatement();
        s.addTableName("skod");
        s.addConstrain("VoldsomVolvo", WhereSQLStatement.EQUAL, "cygnus year");
        s.addJoin("VoldsomVolvo", "cygnus year");
        //System.out.println(s.makeStatement());
    }
}

