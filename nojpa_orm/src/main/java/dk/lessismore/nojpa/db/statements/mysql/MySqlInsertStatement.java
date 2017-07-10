package dk.lessismore.nojpa.db.statements.mysql;

import dk.lessismore.nojpa.db.SQLStatementExecutor;
import dk.lessismore.nojpa.db.statements.*;
import dk.lessismore.nojpa.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public class MySqlInsertStatement extends MySqlStatement implements InsertSQLStatement {

    private static Logger log = LoggerFactory.getLogger(SQLStatementExecutor.class);

    //    private Map<String, String> attributeValues = new HashMap<String, String>();
    private Map<String, Pair<Object, Class>> attributeValuesAndTypes = new HashMap<String, Pair<Object, Class>>();

//    protected Map<String, String> getAttributeValues() {
//        return attributeValues;
//    }

    public Map<String, Pair<Object, Class>> getAttributeValuesAndTypes() {
        return attributeValuesAndTypes;
    }

    public void addAttributeValue(String attributeName, int value) {
//        attributeValues.put(attributeName, MySqlUtil.convertToSql(value));
        attributeValuesAndTypes.put(attributeName, new Pair<>(value, Integer.class));
    }

    public void addAttributeValue(String attributeName, double value) {
//        attributeValues.put(attributeName, MySqlUtil.convertToSql(value));
        attributeValuesAndTypes.put(attributeName, new Pair<>(value, Double.class));
    }

    public void addAttributeValue(String attributeName, float value) {
//        attributeValues.put(attributeName, MySqlUtil.convertToSql(value));
        attributeValuesAndTypes.put(attributeName, new Pair<>(value, Float.class));
    }

    public void addAttributeValue(String attributeName, long value) {
//        attributeValues.put(attributeName, MySqlUtil.convertToSql(value));
        attributeValuesAndTypes.put(attributeName, new Pair<>(value, Long.class));
    }

    public void addAttributeValue(String attributeName, boolean value) {
//        attributeValues.put(attributeName, MySqlUtil.convertToSql(value));
        attributeValuesAndTypes.put(attributeName, new Pair<>(value, Boolean.class));
    }

    public void addAttributeValue(String attributeName, Calendar value) {
//        attributeValues.put(attributeName, MySqlUtil.convertToSql(value));
        attributeValuesAndTypes.put(attributeName, new Pair<>(value, Calendar.class));
    }

    public void addAttributeValue(String attributeName, String value) {
//        attributeValues.put(attributeName, MySqlUtil.convertToSql(value));
        attributeValuesAndTypes.put(attributeName, new Pair<>(value, String.class));
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
        StringBuilder valuesQuestions = new StringBuilder();
        statement.append("INSERT INTO ").append(getTableNames().get(0)).append(" ");
        statement.append("(");
        valuesQuestions.append("(");
        if (!attributeValuesAndTypes.isEmpty()) {
            Iterator iterator = attributeValuesAndTypes.keySet().iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                if (i > 0) {
                    statement.append(", ");
                    valuesQuestions.append(",");
                }
                statement.append(" ").append(iterator.next());
                valuesQuestions.append("?");
            }
        }
        statement.append(")");
        valuesQuestions.append(")");
        statement.append(" VALUES ").append(valuesQuestions);
        return statement.toString();
    }

    public String makeStatement() {
        preCheck();


        StringBuilder statement = new StringBuilder();
        statement.append("insert into ").append(getTableNames().get(0)).append(" (");

        statement.append(makeList(attributeValuesAndTypes.keySet().iterator()));
        statement.append(") ");
        statement.append(" values (");
        {
            Iterator<String> iterator = attributeValuesAndTypes.keySet().iterator();
            for (int i = 0; iterator.hasNext(); i++) {
                if (i > 0) {
                    statement.append(", ");
                }
                Pair<Object, Class> valueTypePair = attributeValuesAndTypes.get(iterator.next());
                statement.append(MySqlUtil.convertToSql(valueTypePair.getFirst(), valueTypePair.getSecond()));
            }
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
