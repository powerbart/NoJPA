package dk.lessismore.nojpa.db.statements;

/**
 * This interface defines the functionality of an select sql statement.
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public interface SelectSQLStatement extends WhereSQLStatement, SQLStatement {

    /**
     * Indicates that the result should be sorted in descending order.
     */
    public static final int DESC = 0;

    /**
     * Indicates that the result should be sorted in assending order.
     */
    public static final int ASC = 1;

    public void removeAttributeName(String attributeName);

    public void addAttributeName(String attributeName);

    public void addAttributeName(String tableName, String attributeName);

    public void addAttributeName(String tableName, String attributeName, String alias);

    public void addLimit(int start, int end);

    public void setOrderBy(String tableName, String attributeName, int sortType);

    public void setOrderBy(String attributeName, int sortType);

    public void setGroupBy(String tableName, String attributeName);

    public void setGroupBy(String attributeName);

    public void setHaving(String having);

    public String makePreparedStatement(PreparedSQLStatement preparedSQLStatement);
}
