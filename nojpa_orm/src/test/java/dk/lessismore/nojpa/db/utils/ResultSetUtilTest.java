package dk.lessismore.nojpa.db.utils;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.db.model.DbStripTest;
import dk.lessismore.nojpa.reflection.db.DatabaseCreator;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created : by IntelliJ IDEA.
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

    @Test
    public void testStrips() throws Exception {
        List<Class> classes = new ArrayList<>();
        classes.add(DbStripTest.class);
        DatabaseCreator.createDatabase(classes, new Class[0]);

        DbStripTest strip = ModelObjectService.create(DbStripTest.class);
        String str = "nasko|<>/\\&?.";
        strip.setMyString(str);
        strip.setNonStripped(str);
        strip.setOneUrl(str);


        assertFalse(strip.isDirty());
        assertTrue(strip.isNew());

        ModelObjectService.save(strip);

        assertEquals("Nasko|`?.", strip.getMyString());
        assertEquals("nasko|<>/`&?.", strip.getNonStripped());
        assertEquals(str, strip.getOneUrl());

        strip.setMyString(str);
        strip.setNonStripped(str);
        strip.setOneUrl(str);

        assertFalse(strip.isDirty());
        assertFalse(strip.isNew());

        assertEquals("Nasko|`?.", strip.getMyString());
        assertEquals("nasko|<>/`&?.", strip.getNonStripped());
        assertEquals(str, strip.getOneUrl());

        ModelObjectService.save(strip);

        assertEquals("Nasko|`?.", strip.getMyString());
        assertEquals("nasko|<>/`&?.", strip.getNonStripped());
        assertEquals(str, strip.getOneUrl());

        DbStripTest strip2 = MQL.selectByID(DbStripTest.class, strip.getObjectID());

        assertEquals("Nasko|`?.", strip2.getMyString());
        assertEquals("nasko|<>/`&?.", strip2.getNonStripped());
        assertEquals(str, strip2.getOneUrl());

    }


}
