package dk.lessismore.nojpa.db.utils;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: seb
 * Date: 22-02-2011
 * Time: 14:40:16
 * To change this template use File | Settings | File Templates.
 */
public class ResultSetUtilTest {


//    @Test
//     public void testResultSetToString() throws Exception {
//       LimResultSet limSet = SQLStatementExecutor.doQuery("select count(*) as counter, HOUR(clickedDate) as hour from _EmailSucces where creationDate > '2011-02-22' and clickedDate IS NOT NULL group by hour order by hour  limit 0, 50");
//       ResultSet resultSet = limSet.getResultSet();
//       StringBuilder builder = ResultSetUtil.resultSetToString(resultSet);
//       resultSet.close();
//       resultSet = null;
//       SuperIO.writeTextToFile("/tmp/testResultSetToString.html", builder.toString());
//
//
//    }

    @Test
    public void testFun() throws Exception {
        assertEquals(1, 1);

    }


}
