package dk.lessismore.nojpa.db.statements.h2;

import dk.lessismore.nojpa.db.statements.Expression;
import dk.lessismore.nojpa.db.statements.UpdateSQLStatement;

import java.util.Calendar;
import java.util.Iterator;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class H2UpdateStatement extends H2InsertStatement implements UpdateSQLStatement {

    H2WhereStatement whereStatement = null;

    protected H2WhereStatement getWhereStatement() {
        if (whereStatement == null) {
            whereStatement = new H2WhereStatement();
        }
        return whereStatement;
    }

    public void addJoin(String attributeName1, String attributeName2) {
        getWhereStatement().addJoin(attributeName1, attributeName2);
    }

    public void addJoin(String tableName1, String attributeName1, String tableName2, String attributeName2) {
        getWhereStatement().addJoin(tableName1, attributeName1, tableName2, attributeName2);
    }

    public void addConstrain(String attributeName, int comparator, String value) {
        getWhereStatement().addConstrain(attributeName, comparator, value);
    }

//    public void whereIn(String attributeName, String[] values) {
//        getWhereStatement().whereIn(attributeName, values);
//    }

    public void addConstrain(String attributeName, int comparator, int value) {
        getWhereStatement().addConstrain(attributeName, comparator, value);
    }

    public void addConstrain(String attributeName, int comparator, double value) {
        getWhereStatement().addConstrain(attributeName, comparator, value);
    }

    public void addConstrain(String attributeName, int comparator, float value) {
        getWhereStatement().addConstrain(attributeName, comparator, value);
    }

    public void addConstrain(String attributeName, int comparator, Calendar value) {
        getWhereStatement().addConstrain(attributeName, comparator, value);
    }

    public void isNull(String attributeName) {
        getWhereStatement().isNull(attributeName);
    }

    public void isNotNull(String attributeName) {
        getWhereStatement().isNotNull(attributeName);
    }

    public void addExpression(Expression expression) {
        getWhereStatement().addExpression(expression);
    }

    public void addExpression(int condition, Expression expression) {
        getWhereStatement().addExpression(condition, expression);
    }

    public String makeStatement() {

        if (getTableNames().isEmpty()) {
            throw new RuntimeException("Carnt make insert statement without tablename");
        }
        if (getAttributeValues().isEmpty()) {
            throw new RuntimeException("Carnt make insert statement without attribute values");
        }


        StringBuilder statement = new StringBuilder();
        statement.append("update");
        Iterator iterator = getTableNames().iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            if (i > 0) {
                statement.append(", ");
            }
            statement.append("\n\t").append(iterator.next());
        }
        statement.append("\nset");
        iterator = getAttributeValues().keySet().iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            if (i > 0) {
                statement.append(",");
            }
            String attributeName = (String) iterator.next();
            String attributeValue = (String) getAttributeValues().get(attributeName);
            statement.append("\n\t").append(attributeName).append(" = ").append(attributeValue);
        }
        statement.append("\n").append(getWhereStatement().makeStatement());
        return statement.toString();
    }

    public static void main(String[] args) {
        H2UpdateStatement s = new H2UpdateStatement();
        s.addTableName("skod");
        s.addTableName("skod2");
        s.addAttributeValue("VoldsomVolvo", "hej med dig");
        s.addAttributeValue("int", 12);
        s.addAttributeValue("double", 12.12);
        s.addAttributeValue("Date", Calendar.getInstance());
        /*s.addConstrain("VoldsomVolvo", WhereSQLStatement.EQUAL, "cygnus year");
        s.addJoin("VoldsomVolvo", "cygnus year");*/
        //System.out.println(s.makeStatement());
    }
}
