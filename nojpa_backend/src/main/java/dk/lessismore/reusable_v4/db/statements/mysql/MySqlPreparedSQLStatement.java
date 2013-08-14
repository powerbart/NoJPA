package dk.lessismore.reusable_v4.db.statements.mysql;

import dk.lessismore.reusable_v4.db.statements.*;

import javax.sql.rowset.serial.SerialArray;
import java.util.*;
import java.sql.*;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class MySqlPreparedSQLStatement implements PreparedSQLStatement {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MySqlPreparedSQLStatement.class);
    private List listOfLeafs = new LinkedList();

    public void addLeafExpression(LeafExpression expression) {
        //log.debug("addLeafExpression: " + ((MySqlLeafExpression) expression).getPreparedStatement());
        listOfLeafs.add(expression);
    }

    public void makeStatementReadyToExcute(PreparedStatement statement) throws SQLException {
        for (int i = 0; i < listOfLeafs.size(); i++) {
            //log.debug("adding " + (i + 1) + "leaf:" + listOfLeafs.get(i) +" = " + ((LeafExpression) listOfLeafs.get(i)).getPreparedValue());
            statement.setString(i + 1, ((LeafExpression) listOfLeafs.get(i)).getPreparedValue());
        }
    }
}
