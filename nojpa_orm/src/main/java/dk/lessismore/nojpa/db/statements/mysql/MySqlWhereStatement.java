package dk.lessismore.nojpa.db.statements.mysql;

import dk.lessismore.nojpa.db.statements.ContainerExpression;
import dk.lessismore.nojpa.db.statements.Expression;
import dk.lessismore.nojpa.db.statements.LeafExpression;
import dk.lessismore.nojpa.db.statements.PreparedSQLStatement;
import dk.lessismore.nojpa.db.statements.WhereSQLStatement;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public class MySqlWhereStatement extends MySqlStatement implements WhereSQLStatement {

    protected ContainerExpression basicContainerExpression = new MySqlContainerExpression();
    protected ContainerExpression containerExpression = new MySqlContainerExpression();
    protected List constrains = null;

    public MySqlWhereStatement() {

    }

    protected List getConstrains() {
        if (constrains == null) {
            constrains = new LinkedList();
        }
        return constrains;
    }

    public void addJoin(String attributeName1, String attributeName2) {
        LeafExpression leafExpression = new MySqlLeafExpression();
        leafExpression.addJoin(attributeName1, attributeName2);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }

    public void addJoin(String tableName1, String attributeName1, String tableName2, String attributeName2) {

        LeafExpression leafExpression = new MySqlLeafExpression();
        leafExpression.addJoin(tableName1, attributeName1, tableName2, attributeName2);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }

    public void addConstrain(String attributeName, int comparator, String value) {
        LeafExpression leafExpression = new MySqlLeafExpression();
        leafExpression.addConstrain(attributeName, comparator, value);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }

//    public void whereIn(String attributeName, String[] values) {
//        LeafExpression leafExpression = new MySqlLeafExpression();
//        leafExpression.whereIn(attributeName, values);
//        basicContainerExpression.addExpression(leafExpression);
//        basicContainerExpression.addCondition(WhereSQLStatement.AND);
//    }

    public void addConstrain(String attributeName, int comparator, Calendar value) {
        LeafExpression leafExpression = new MySqlLeafExpression();
        leafExpression.addConstrain(attributeName, comparator, value);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }


    public void addConstrain(String attributeName, int comparator, int value) {
        LeafExpression leafExpression = new MySqlLeafExpression();
        leafExpression.addConstrain(attributeName, comparator, value);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }

    public void addConstrain(String attributeName, int comparator, double value) {
        LeafExpression leafExpression = new MySqlLeafExpression();
        leafExpression.addConstrain(attributeName, comparator, value);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }

    public void addConstrain(String attributeName, int comparator, float value) {
        LeafExpression leafExpression = new MySqlLeafExpression();
        leafExpression.addConstrain(attributeName, comparator, value);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }

    public void isNull(String attributeName) {
        LeafExpression leafExpression = new MySqlLeafExpression();
        leafExpression.isNull(attributeName);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }

    public void isNotNull(String attributeName) {
        LeafExpression leafExpression = new MySqlLeafExpression();
        leafExpression.isNotNull(attributeName);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }

    public void addExpression(Expression expression) {
        containerExpression.addExpression(expression);
    }

    public void addExpression(int condition, Expression expression) {
        containerExpression.addExpression(expression);
        containerExpression.addCondition(condition);
    }

    public String makeStatement() {
//        log.debug("makeStatement():1");
        if (containerExpression.nrOfExpressions() == 0 && basicContainerExpression.nrOfExpressions() == 0) {
            return "";
        }

        ContainerExpression pt = new MySqlContainerExpression();
        pt.addExpression(basicContainerExpression);
        pt.addCondition(WhereSQLStatement.AND);
        pt.addExpression(containerExpression);
//        pt.addCondition(WhereSQLStatement.AND);

        String expression = pt.makeStatement();
        //log.debug("expression='"+ expression +"'");

//        log.debug("makeStatement():2");
//        if (containerExpression.nrOfExpressions() == 0) {
//            containerExpression = basicContainerExpression;
//        } else if (basicContainerExpression.nrOfExpressions() > 0 && !haveAddBasic) {
//            haveAddBasic = true;
//            containerExpression.addExpression(basicContainerExpression);
//            containerExpression.addCondition(WhereSQLStatement.AND);
//        }
//        log.debug("makeStatement():3");
//        String expression = containerExpression.makeStatement();
//        log.debug("makeStatement():4");
        if (expression.isEmpty()) {
            return "";
        }
//        log.debug("makeStatement():5");
        return "where " + expression;
    }

    private boolean haveAddBasic = false;

    public String makePreparedStatement(PreparedSQLStatement preparedSQLStatement) {
        if (containerExpression.nrOfExpressions() == 0 && basicContainerExpression.nrOfExpressions() == 0) {
            return "";
        }
        if (containerExpression.nrOfExpressions() == 0) {
            containerExpression = basicContainerExpression;
        } else if (basicContainerExpression.nrOfExpressions() > 0 && !haveAddBasic) {
            haveAddBasic = true;
            containerExpression.addExpression(basicContainerExpression);
            containerExpression.addCondition(WhereSQLStatement.AND);
        }
        String expression = containerExpression.makePreparedStatement(preparedSQLStatement);
        if (expression.isEmpty()) {
            return "";
        }
        return "where " + expression;
    }
}
