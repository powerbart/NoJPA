package dk.lessismore.nojpa.db.statements.h2;

import dk.lessismore.nojpa.db.statements.*;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public class H2WhereStatement extends H2Statement implements WhereSQLStatement {

    private final static Logger log = Logger.getLogger(H2WhereStatement.class);

    protected ContainerExpression basicContainerExpression = new H2ContainerExpression();
    protected ContainerExpression containerExpression = new H2ContainerExpression();
    protected List constrains = null;

    public H2WhereStatement() {

    }

    protected List getConstrains() {
        if (constrains == null) {
            constrains = new LinkedList();
        }
        return constrains;
    }

    public void addJoin(String attributeName1, String attributeName2) {
        LeafExpression leafExpression = new H2LeafExpression();
        leafExpression.addJoin(attributeName1, attributeName2);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }

    public void addJoin(String tableName1, String attributeName1, String tableName2, String attributeName2) {

        LeafExpression leafExpression = new H2LeafExpression();
        leafExpression.addJoin(tableName1, attributeName1, tableName2, attributeName2);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }

    public void addConstrain(String attributeName, int comparator, String value) {
        LeafExpression leafExpression = new H2LeafExpression();
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
        LeafExpression leafExpression = new H2LeafExpression();
        leafExpression.addConstrain(attributeName, comparator, value);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }


    public void addConstrain(String attributeName, int comparator, int value) {
        LeafExpression leafExpression = new H2LeafExpression();
        leafExpression.addConstrain(attributeName, comparator, value);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }

    public void addConstrain(String attributeName, int comparator, double value) {
        LeafExpression leafExpression = new H2LeafExpression();
        leafExpression.addConstrain(attributeName, comparator, value);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }

    public void addConstrain(String attributeName, int comparator, float value) {
        LeafExpression leafExpression = new H2LeafExpression();
        leafExpression.addConstrain(attributeName, comparator, value);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }

    public void isNull(String attributeName) {
        LeafExpression leafExpression = new H2LeafExpression();
        leafExpression.isNull(attributeName);
        basicContainerExpression.addExpression(leafExpression);
        basicContainerExpression.addCondition(WhereSQLStatement.AND);
    }

    public void isNotNull(String attributeName) {
        LeafExpression leafExpression = new H2LeafExpression();
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

        ContainerExpression pt = new H2ContainerExpression();
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
