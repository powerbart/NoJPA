package dk.lessismore.nojpa.db.statements.mysql;

import dk.lessismore.nojpa.db.statements.*;

import java.util.*;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public abstract class MySqlStatement implements SQLStatement {

    protected List tableNames = null;

    public void addTableName(String tableName) {
        if (!getTableNames().contains(tableName)) {
            getTableNames().add(tableName);
        }
    }

    protected List getTableNames() {
        if (tableNames == null) {
            tableNames = new LinkedList();
        }
        return tableNames;
    }

    public abstract String makeStatement();

    public String makeList(Iterator iterator) {
        return makeList(iterator, ", ");
    }

    public String makeList(Iterator iterator, String separator) {
        StringBuilder list = new StringBuilder();

        for (int i = 0; iterator.hasNext(); i++) {
            if (i > 0) {
                list.append(separator);
            }
            list.append("\n\t").append(iterator.next());
        }
        return list.toString();
    }
}
