package dk.lessismore.nojpa.db.statements.mysql;

import dk.lessismore.nojpa.db.statements.*;
import dk.lessismore.nojpa.utils.Pair;

import java.util.*;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public class MySqlUpdateStatement extends MySqlInsertStatement implements UpdateSQLStatement {

    MySqlWhereStatement whereStatement = null;

    protected MySqlWhereStatement getWhereStatement() {
        if (whereStatement == null) {
            whereStatement = new MySqlWhereStatement();
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




    @Override
    protected void preCheck(){
        super.preCheck();
        if (getAttributeValuesAndTypes().isEmpty()) {
            throw new RuntimeException("Can't make insert statement without attribute values");
        }
    }


    @Override
    public String makePreparedStatement(PreparedSQLStatement preSQLStatement) {
        preCheck();
        StringBuilder statement = new StringBuilder();
        statement.append("UPDATE ").append(getTableNames().get(0)).append(" SET ");
        if (!getAttributeValuesAndTypes().isEmpty()) {
            Iterator iterator = getAttributeValuesAndTypes().keySet().iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                if (i > 0) {
                    statement.append(", ");
                }
                statement.append(" ").append(iterator.next()).append("=?");
            }
        }
        statement.append(" ");
        statement.append(getWhereStatement().makeStatement());
//        statement.append(getWhereStatement().makePreparedStatement(preSQLStatement));
        return statement.toString();
    }

    public String makeStatement() {
        preCheck();

        StringBuilder statement = new StringBuilder();
        statement.append("update ");
        statement.append(tableList());
        statement.append("\nset ");
        Iterator<String> iterator = getAttributeValuesAndTypes().keySet().iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            if (i > 0) {
                statement.append(",");
            }
            String attributeName = (String) iterator.next();
            Pair<Object, Class> objectClassPair = getAttributeValuesAndTypes().get(attributeName);
            String attributeValue = (String) MySqlUtil.convertToSql(objectClassPair.getFirst(), objectClassPair.getSecond());
            statement.append("\n\t").append(attributeName).append(" = ").append(attributeValue);
        }
        statement.append("\n").append(getWhereStatement().makeStatement());
        return statement.toString();
    }

    public static void main(String[] args) {
        MySqlUpdateStatement s = new MySqlUpdateStatement();
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
