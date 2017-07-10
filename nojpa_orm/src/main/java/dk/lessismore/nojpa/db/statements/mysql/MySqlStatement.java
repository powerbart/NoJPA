package dk.lessismore.nojpa.db.statements.mysql;

import dk.lessismore.nojpa.db.statements.SQLStatement;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public abstract class MySqlStatement implements SQLStatement {

    protected List<String> tableNames = null;

    public void addTableName(String tableName) {
        if (!getTableNames().contains(tableName)) {
            getTableNames().add(tableName);
        }
    }

    protected List<String> getTableNames() {
        if (tableNames == null) {
            tableNames = new LinkedList<String>();
        }
        return tableNames;
    }

    public abstract String makeStatement();

    protected void preCheck(){
        if (getTableNames().isEmpty()) {
            throw new RuntimeException("Can't make statement without tablename");
        }
    }

    protected String tableList() {
        return makeList(tableNames.iterator());
    }

    protected String makeList(Iterator iterator) {
        return makeList(iterator, ", ");
    }

    protected String makeList(Iterator iterator, String separator) {
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
