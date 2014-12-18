package dk.lessismore.nojpa.db.statements.oracle;

import dk.lessismore.nojpa.db.statements.ContainerExpression;
import dk.lessismore.nojpa.db.statements.Expression;
import dk.lessismore.nojpa.db.statements.PreparedSQLStatement;
import dk.lessismore.nojpa.db.statements.WhereSQLStatement;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public class OracleContainerExpression implements ContainerExpression {

//    private final static org.apache.log4j.Logger log = Logger.getLogger(MySqlContainerExpression.class);

    List<Expression> expressions = new LinkedList<Expression>();
    List<String> conditions = new LinkedList<String>();

    public ContainerExpression addExpression(Expression expression) {
        expressions.add(expression);
        return this;
    }

    public ContainerExpression addExpression(int condition, Expression expression) {
        addExpression(expression);
        addCondition(condition);
        return this;
    }

    public ContainerExpression addCondition(int condition) {
        conditions.add(WhereSQLStatement.conditionAsStr[condition]);
        return this;
    }

//    public ContainerExpression reAddCondition(int condition) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
    public int nrOfExpressions() {
        int n = 0;
        for (Object expression : expressions) {
            if (expression instanceof ContainerExpression) {
                n += ((ContainerExpression) expression).nrOfExpressions();
            } else {
                n++;
            }
        }
        return n;
    }

    public String makeStatement() {
//        log.debug("makeStatement():1");
//        Thread.dumpStack();
        if (expressions.isEmpty()) {
            return "";
        }

        List<String> statements = new LinkedList<String>();
        String statement = "";
        for (Expression expression : expressions) {

            String s = expression.makeStatement().trim();
            if (!s.equals("")) {
                statements.add(s);
            }
        }
        Iterator statementsIterator = statements.iterator();
        for (int i = 0; statementsIterator.hasNext(); i++) {
            String stmt = (String) statementsIterator.next();
            if (i > 0) {
                String condition = WhereSQLStatement.conditionAsStr[WhereSQLStatement.AND];
                if ((i - 1) < conditions.size()) {
                    condition = conditions.get((i - 1));
                }
                statement = statement + " " + condition + "\n\t" + stmt;
//                log.debug("tmp.1.statement = " + statement);
            } else {
                statement = "\n\t" + stmt;
            }
//            log.debug("tmp.2.statement = " + statement);
        }

        if (expressions.size() > 1) {
            return "(" + statement + ")";
        } else {
            return statement;
        }
    }

//    public int getLastCondition() {
//        return 0;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    public void setLastCondition(int lastCondition) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }


    public String makePreparedStatement(PreparedSQLStatement preparedSQLStatement) {

        if (expressions.isEmpty()) {
            return "";
        }

        List<String> statements = new LinkedList<String>();
        String statement = "";
        for (Expression expression : expressions) {
            String s = expression.makePreparedStatement(preparedSQLStatement);
            //System.out.println("makePreparedStatement: adding " + s);
            statements.add(s);
        }
        Iterator statementsIterator = statements.iterator();
        for (int i = 0; statementsIterator.hasNext(); i++) {
            String stmt = (String) statementsIterator.next();
            if (i > 0) {
                String condition = WhereSQLStatement.conditionAsStr[WhereSQLStatement.AND];
                if ((i - 1) < conditions.size()) {
                    condition = conditions.get((i - 1));
                }
                statement = statement + " " + condition + "\n\t" + stmt;
                //log.debug("tmp.1.statement = " + statement);
            } else {
                statement = "\n\t" + stmt;
            }
            //log.debug("tmp.2.statement = " + statement);
        }

        if (expressions.size() > 1) {
            return "(" + statement + ")";
        } else {
            return statement;
        }
    }
}
