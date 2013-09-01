package dk.lessismore.nojpa.db.utils;

import dk.lessismore.nojpa.db.LimResultSet;
import dk.lessismore.nojpa.db.SQLStatementExecutor;
import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.db.statements.SelectSQLStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;

/**
 * Created by IntelliJ IDEA.
 * User: seb
 * Date: 22-02-2011
 * Time: 13:32:56
 * To change this template use File | Settings | File Templates.
 */
public class ResultSetUtil {


    public static StringBuilder queryToHtmlTable(MQL.SelectQuery selectQuery) throws SQLException {
        SelectSQLStatement statement = selectQuery.getSelectSQLStatement();
        LimResultSet limSet = SQLStatementExecutor.doQuery(statement);
        ResultSet resultSet = limSet.getResultSet();
        StringBuilder builder = ResultSetUtil.resultSetToString(resultSet);
        resultSet.close();
        resultSet = null;
        limSet.close();
        return builder;
    }


    public static StringBuilder queryToHtmlTable(String query) throws SQLException {
       LimResultSet limSet = SQLStatementExecutor.doQuery(query);
       ResultSet resultSet = limSet.getResultSet();
       StringBuilder builder = ResultSetUtil.resultSetToString(resultSet);
       resultSet.close();
       resultSet = null;
       limSet.close();
       return builder;
    }


    public static StringBuilder resultSetToString(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int countOfColumns = metaData.getColumnCount();
        StringBuilder builder = new StringBuilder();
        builder.append("<table>");
        builder.append("<tr>");
        for(int i = 0; i < countOfColumns; i++){
            builder.append("<th>");
            builder.append(metaData.getColumnName(i+1));
            builder.append("</th>");
        }
        builder.append("</tr>");

        while(resultSet.next()){
            builder.append("<tr>");
            for(int i = 0; i < countOfColumns; i++){
                builder.append("<td>");
                builder.append(resultSet.getString(i+1));
                builder.append("</td>");
            }
            builder.append("</tr>");
        }
        builder.append("</table>");
        return builder;

    }





}
