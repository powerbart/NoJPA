package dk.lessismore.nojpa.db.statements.mysql;

import java.util.*;

import dk.lessismore.nojpa.db.statements.*;
import dk.lessismore.nojpa.reflection.db.DbClassReflector;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttribute;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttributeContainer;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class MySqlLeafExpression implements LeafExpression {

    String statement = "";
    String preparedStatement = "";
    String preparedValue = "";
    boolean isJoin = false;

    public LeafExpression addJoin(String attributeName1, String attributeName2) {
        statement = attributeName1 + " = " + attributeName2;
        preparedStatement = attributeName1 + " = " + attributeName2;
        isJoin = true;
        return this;
    }

    public LeafExpression addJoin(String tableName1, String attributeName1, String tableName2, String attributeName2) {
        statement = tableName1 + "." + attributeName1 + " = " + tableName2 + "." + attributeName2;
        preparedStatement = tableName1 + "." + attributeName1 + " = " + tableName2 + "." + attributeName2;
        isJoin = true;
        return this;
    }

    public LeafExpression addConstrain(String attributeName, int comparator, String value) {
        statement = attributeName + " " + WhereSQLStatement.comparatorAsStr[comparator] + " " + MySqlUtil.convertToSql(value);
        preparedStatement = attributeName + " " + WhereSQLStatement.comparatorAsStr[comparator] + " ?";
        preparedValue = MySqlUtil.convertToPreparedSql(value);
        return this;
    }

//    public LeafExpression whereIn(String attributeName, String[] values) {
//        statement = attributeName + " IN " + MySqlUtil.convertToSql(values);
//        preparedStatement = attributeName + " IN ?";
//        preparedValue = MySqlUtil.convertToPreparedSql(values);
//        return this;
//    }

    public LeafExpression addConstrain(String attributeName, int comparator, Calendar value) {
        statement = attributeName + " " + WhereSQLStatement.comparatorAsStr[comparator] + " " + MySqlUtil.convertToSql(value);
        preparedStatement = attributeName + " " + WhereSQLStatement.comparatorAsStr[comparator] + " ?";
        preparedValue = MySqlUtil.convertToPreparedSql(value);
        return this;
    }

    public LeafExpression addConstrain(String attributeName, int comparator, int value) {
        statement = attributeName + " " + WhereSQLStatement.comparatorAsStr[comparator] + " " + MySqlUtil.convertToSql(value);
        preparedStatement = attributeName + " " + WhereSQLStatement.comparatorAsStr[comparator] + " ?";
        preparedValue = MySqlUtil.convertToPreparedSql(value);
        return this;
    }

    public LeafExpression addConstrain(String attributeName, int comparator, double value) {
        statement = attributeName + " " + WhereSQLStatement.comparatorAsStr[comparator] + " " + MySqlUtil.convertToSql(value);
        preparedStatement = attributeName + " " + WhereSQLStatement.comparatorAsStr[comparator] + " ?";
        preparedValue = MySqlUtil.convertToPreparedSql(value);
        return this;
    }

    public LeafExpression addConstrain(String attributeName, int comparator, float value) {
        statement = attributeName + " " + WhereSQLStatement.comparatorAsStr[comparator] + " " + MySqlUtil.convertToSql(value);
        preparedStatement = attributeName + " " + WhereSQLStatement.comparatorAsStr[comparator] + " ?";
        preparedValue = MySqlUtil.convertToPreparedSql(value);
        return this;
    }

    public LeafExpression addConstrain(String attributeName, int comparator, long value) {
        statement = attributeName + " " + WhereSQLStatement.comparatorAsStr[comparator] + " " + MySqlUtil.convertToSql(value);
        preparedStatement = attributeName + " " + WhereSQLStatement.comparatorAsStr[comparator] + " ?";
        preparedValue = MySqlUtil.convertToPreparedSql(value);
        return this;
    }

    @Override
    public LeafExpression addConstrain(Class sourceClass, String attributeName, int comparator, String value) {
        return addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
    }

    @Override
    public LeafExpression addConstrain(Class sourceClass, String attributeName, int comparator, Calendar value) {
        return addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
    }

    @Override
    public LeafExpression addConstrain(Class sourceClass, String attributeName, int comparator, int value) {
        return addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
    }

    @Override
    public LeafExpression addConstrain(Class sourceClass, String attributeName, int comparator, double value) {
        return addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
    }

    @Override
    public LeafExpression addConstrain(Class sourceClass, String attributeName, int comparator, float value) {
        return addConstrain(makeAttributeIdentifier(sourceClass, attributeName), comparator, value);
    }

    private String makeAttributeIdentifier(Class sourceClass, String attributeName) {
        DbAttributeContainer dbAttributeContainer = DbClassReflector.getDbAttributeContainer(sourceClass);
        DbAttribute dbAttribute = dbAttributeContainer.getDbAttribute(attributeName);
        return dbAttributeContainer.getTableName()+"."+dbAttribute.getAttributeName();
    }

    public LeafExpression isNull(String attributeName) {
        statement = attributeName + " is null";
        preparedStatement = attributeName + " is null";
        preparedValue = null;
        return this;
    }

    public LeafExpression isNotNull(String attributeName) {
        statement = attributeName + " is not null";
        preparedStatement = attributeName + " is not null";
        preparedValue = null;
        return this;
    }

    @Override
    public LeafExpression setToFalse() {
        statement = " false";
        preparedStatement = statement;
        preparedValue = null;
        return this;
    }

    public String makePreparedStatement(PreparedSQLStatement preparedSQLStatement) {
        if (!isJoin) {
            preparedSQLStatement.addLeafExpression(this);
        }
        return preparedStatement;
    }

    public String makeStatement() {
        return statement;
    }

    public int getLastCondition() {
        return  WhereSQLStatement.AND;
    }

    public void setLastCondition(int lastCondition){
        //Nothing
    }

    public String getPreparedValue() {
        return preparedValue;
    }

    public String getPreparedStatement() {
        return preparedStatement;
    }

}
