package dk.lessismore.nojpa.db.statements.h2;

import dk.lessismore.nojpa.db.statements.LeafExpression;
import dk.lessismore.nojpa.db.statements.PreparedSQLStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Mysql statement implementation
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class H2PreparedSQLStatement implements PreparedSQLStatement {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(H2PreparedSQLStatement.class);
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
