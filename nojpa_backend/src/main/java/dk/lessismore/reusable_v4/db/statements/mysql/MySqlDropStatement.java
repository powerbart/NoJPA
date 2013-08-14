package dk.lessismore.reusable_v4.db.statements.mysql;

import dk.lessismore.reusable_v4.db.statements.*;

import java.util.*;
import java.text.*;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class MySqlDropStatement extends MySqlStatement implements DropSQLStatement {

    public String makeStatement() {

        if (getTableNames().isEmpty()) {
            throw new RuntimeException("Carnt make drop statement without tablename");
        }
        return "drop table if exists " + getTableNames().get(0);
    }

}
