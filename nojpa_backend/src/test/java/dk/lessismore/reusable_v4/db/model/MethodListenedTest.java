package dk.lessismore.reusable_v4.db.model;

import dk.lessismore.reusable_v4.cache.ObjectCacheFactory;
import dk.lessismore.reusable_v4.db.SQLStatementExecutor;
import dk.lessismore.reusable_v4.db.methodquery.MQ;
import dk.lessismore.reusable_v4.db.statements.SQLStatement;
import dk.lessismore.reusable_v4.reflection.db.DatabaseCreator;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectService;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: niakoi
 * Date: 7/29/13
 * Time: 5:23 PM
 */
public class MethodListenedTest {

    @BeforeClass
    public static void setUp() {
        List<SQLStatement> tables = new LinkedList<SQLStatement>();

        tables.addAll(DatabaseCreator.makeTableFromClass(MethodListened.class));
        for(SQLStatement sqlStatement: tables) {
            String sql = sqlStatement.makeStatement();
            SQLStatementExecutor.doUpdate(sql);
        }
    }

    @Test
    public void testInvocationWrite() {

        MethodListened methodListened = ModelObjectService.create(MethodListened.class);
        methodListened.setMyValue("Nasko");
        ModelObjectService.save(methodListened);

        Assert.assertEquals("Nasko_", methodListened.getMyValue());

    }

    @Test
    public void testInvocationRead() {
        MethodListened methodListened = ModelObjectService.create(MethodListened.class);
        methodListened.setMyValue("Nasko3");
        ModelObjectService.save(methodListened);

        Assert.assertEquals("Nasko3_", methodListened.getMyValue());

        ModelObjectService.save(methodListened);

        ObjectCacheFactory.getInstance().getObjectCache(MethodListened.class).removeFromCache(methodListened.getObjectID());

        MethodListened fromDatabase = MQ.selectByID(MethodListened.class, methodListened.getObjectID());
        Assert.assertEquals("Nasko3__", fromDatabase.getMyValue());

    }
}
