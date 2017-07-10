package dk.lessismore.nojpa.db.statements.mysql;

import dk.lessismore.nojpa.db.statements.PreparedSQLStatement;
import dk.lessismore.nojpa.db.statements.SelectSQLStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE
 * @version 1.0 25-7-02
 */
public class MySqlSelectStatement extends MySqlWhereStatement implements SelectSQLStatement {
    private static final Logger log = LoggerFactory.getLogger(MySqlSelectStatement.class);

    private List<String> attributeNames = null;
    private ArrayList<String> sortAttributeNameList = new ArrayList<String>();
    private ArrayList<String> groupAttributeNameList = new ArrayList<String>();
    private int limitStart = -1;
    private int limitEnd = -1;
//    protected String groupByTablename = null;
//    protected String groupByName = null;
    protected String having = null;


    private List<String> getAttributeNames() {
        if (attributeNames == null) {
            attributeNames = new LinkedList<String>();
        }
        return attributeNames;
    }

    public String toString() {
        return makeStatement();
    }

    public void addLimit(int start, int end) {
//        log.debug("START: addLimit(start("+ start +"), end("+ end +")) ... this.limitStart("+ this.limitStart +") this.limitEnd("+ this.limitEnd +")");
        if(limitStart != -1 && start == 0 && end == 1){
            this.limitEnd = this.limitStart + 1;
        } else {
            this.limitStart = start;
            this.limitEnd = end;
        }
//        log.debug("END: addLimit(start("+ start +"), end("+ end +")) ... this.limitStart("+ this.limitStart +") this.limitEnd("+ this.limitEnd +")");
    }

    public void setGroupBy(String tableName, String attributeName) {
        groupAttributeNameList.add(tableName + "." + attributeName);
    }

    public void setGroupBy(String attributeName) {
        groupAttributeNameList.add(attributeName);
    }

    public void removeAttributeName(String attributeName) {
        getAttributeNames().remove(attributeName);
    }

    public void addAttributeName(String attributeName) {
        getAttributeNames().add(attributeName);
    }

    public void addAttributeName(String tableName, String attributeName) {
        getAttributeNames().add(tableName + "." + attributeName);
    }

    public void addAttributeName(String tableName, String attributeName, String alias) {
        getAttributeNames().add(tableName + "." + attributeName + " as " + alias);
    }

    public void setOrderBy(String tableName, String attributeName, int sortType) {
        sortAttributeNameList.add(tableName + "." + attributeName + " " + ((sortType == DESC) ? "DESC" : "ASC"));
    }

    public void setOrderBy(String attributeName, int sortType) {
        sortAttributeNameList.add(attributeName + " " + ((sortType == DESC) ? "DESC" : "ASC"));
    }

    public void setHaving(String having) {
        this.having = having;
    }

    public String makeStatement() {
        preCheck();

        StringBuilder statement = new StringBuilder();
        statement.append("select ");
        statement.append(makeList(getAttributeNames().iterator()));

        statement.append("\nfrom ");
        statement.append(tableList());
        statement.append("\n").append(super.makeStatement());

        for (int i = 0; !groupAttributeNameList.isEmpty() && i < groupAttributeNameList.size(); i++) {
            if(i == 0){
                statement.append("\nGROUP BY ");
            } else {
                statement.append(", ");
            }
            statement.append(groupAttributeNameList.get(i)).append(" ");
        }
        for (int i = 0; !sortAttributeNameList.isEmpty() && i < sortAttributeNameList.size(); i++) {
            if(i == 0){
                statement.append("\nORDER BY ");
            } else {
                statement.append(", ");
            }
            statement.append(sortAttributeNameList.get(i)).append(" ");
        }
        if (having != null) {
            statement.append("\nHAVING ").append(having);
        }
        if(limitStart < 0 && limitEnd != -1) log.warn("limitStart cannot be negative except when both limits are -1",
                new IllegalArgumentException("Negative limitStart (was " + limitStart + ")"));
        if(limitEnd < 0 && limitStart != -1) log.warn("limitEnd cannot be negative except when both limits are -1",
                new IllegalArgumentException("Negative limitEnd (was " + limitEnd + ")"));
        if (limitStart != -1 && limitEnd != -1) {
            statement.append("\nlimit " + limitStart + ", " + (limitEnd - limitStart));
        }
        return statement.toString();
    }

    public String makePreparedStatement(PreparedSQLStatement preparedSQLStatement) {
        preCheck();

        StringBuilder statement = new StringBuilder();
        statement.append("select ");
        statement.append(makeList(getAttributeNames().iterator()));

        statement.append("\nfrom ");
        statement.append(tableList());
        statement.append("\n").append(super.makePreparedStatement(preparedSQLStatement));

        for (int i = 0; !groupAttributeNameList.isEmpty() && i < groupAttributeNameList.size(); i++) {
            if(i == 0){
                statement.append("\nGROUP BY ");
            } else {
                statement.append(", ");
            }
            statement.append(groupAttributeNameList.get(i)).append(" ");
        }
        for (int i = 0; !sortAttributeNameList.isEmpty() && i < sortAttributeNameList.size(); i++) {
            if(i == 0){
                statement.append("\nORDER BY ");
            } else {
                statement.append(", ");
            }
            statement.append(sortAttributeNameList.get(i)).append(" ");
        }
        if (having != null) {
            statement.append("\nHAVING ").append(having);
        }
        if(limitStart < 0 && limitEnd != -1) log.warn("limitStart cannot be negative except when both limits are -1",
                new IllegalArgumentException("Negative limitStart (was " + limitStart + ")"));
        if(limitEnd < 0 && limitStart != -1) log.warn("limitEnd cannot be negative except when both limits are -1",
                new IllegalArgumentException("Negative limitEnd (was " + limitEnd + ")"));
        if (limitStart != -1 && limitEnd != -1) {
            statement.append("\nlimit ").append(limitStart).append(", ").append(limitEnd - limitStart);
        }
        return statement.toString();
    }

    public static void main(String[] args) {
        /*MySqlSelectStatement s = new MySqlSelectStatement();
        s.addTableName("skod");
        s.addTableName("skod2");
        s.addAttributeName("VoldsomVolvo");
        s.addConstrain("VoldsomVolvo", WhereSQLStatement.EQUAL_AND_LESS, "cygnus year");
        s.addJoin("VoldsomVolvo", "cygnus year");
        System.out.println(s.makeStatement());*/
    }
}

