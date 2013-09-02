package dk.lessismore.nojpa.db.statements.mysql;

import dk.lessismore.nojpa.db.statements.*;

import java.util.*;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class MySqlInsertStatement extends MySqlStatement implements InsertSQLStatement {

    protected Map attributeValues = new HashMap();

    protected Map getAttributeValues() {
        return attributeValues;
    }

    public void addAttributeValue(String attributeName, Object value) {
        attributeValues.put(attributeName, MySqlUtil.convertToSql(value.toString()));
    }

    public void addAttributeValue(String attributeName, int value) {
        attributeValues.put(attributeName, MySqlUtil.convertToSql(value));
    }

    public void addAttributeValue(String attributeName, double value) {
        attributeValues.put(attributeName, MySqlUtil.convertToSql(value));
    }

    public void addAttributeValue(String attributeName, float value) {
        attributeValues.put(attributeName, MySqlUtil.convertToSql(value));
    }

    public void addAttributeValue(String attributeName, long value) {
        attributeValues.put(attributeName, MySqlUtil.convertToSql(value));
    }

    public void addAttributeValue(String attributeName, boolean value) {
        attributeValues.put(attributeName, MySqlUtil.convertToSql(value));
    }

    public void addAttributeValue(String attributeName, Calendar value) {
        attributeValues.put(attributeName, MySqlUtil.convertToSql(value));
    }

    public void addAttributeValue(String attributeName, String value) {
        attributeValues.put(attributeName, MySqlUtil.convertToSql(value));
    }

    public String makeStatement() {
        if (getTableNames().isEmpty()) {
            throw new RuntimeException("Can't make insert statement without tablename");
        }
        if (getAttributeValues().isEmpty()) {
            throw new RuntimeException("Can't make insert statement without attribute values");
        }

        StringBuilder statement = new StringBuilder();
        statement.append("insert into ").append(getTableNames().get(0)).append(" (");

        Iterator iterator = getAttributeValues().keySet().iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            if (i > 0) {
                statement.append(", ");
            }
            statement.append((String) iterator.next());
        }
        statement.append(") ");
        statement.append(" values (");

        iterator = getAttributeValues().keySet().iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            if (i > 0) {
                statement.append(", ");
            }
            statement.append(getAttributeValues().get(iterator.next()));
        }
        statement.append(')');
        return statement.toString();
    }

    public static void main(String[] args) {
        MySqlInsertStatement s = new MySqlInsertStatement();
        s.addTableName("skod");
        s.addAttributeValue("VoldsomVolvo", "hej med dig");
        s.addAttributeValue("int", 12);
        s.addAttributeValue("double", 12.12);
        s.addAttributeValue("Date", Calendar.getInstance());
        //System.out.println(s.makeStatement());
    }
}
