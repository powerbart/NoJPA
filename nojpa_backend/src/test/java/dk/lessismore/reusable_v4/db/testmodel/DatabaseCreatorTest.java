package dk.lessismore.reusable_v4.db.testmodel;

import dk.lessismore.reusable_v4.reflection.db.DatabaseCreator;

import dk.lessismore.reusable_v4.reflection.db.DbClassReflector;
import dk.lessismore.reusable_v4.reflection.db.attributes.DbAttribute;
import dk.lessismore.reusable_v4.reflection.db.attributes.DbAttributeContainer;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: seb
 * Date: 26-03-11
 * Time: 16:52
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseCreatorTest {

    @Test
    public void testCreateTables() {
        ArrayList<Class> cs = new ArrayList<Class>();
        cs.add(Address.class);
        DatabaseCreator.createDatabase(cs);
    }

    @Test
    public void alterCreateTablesSingle() throws Exception {
        DatabaseCreator.alterTableToThisClass(Address.class);
    }

    @Test
    public void alterCreateTables() throws Exception {
        DatabaseCreator.alterDatabase("dk.lessismore.reusable_v4.db.testmodel");
    }


    @Test
    public void testDbAttribute() throws Exception {
        DbAttributeContainer attributeContainer = DbClassReflector.getDbAttributeContainer(Company.class);
        DbAttribute employees = attributeContainer.getDbAttribute("employees");
        System.out.println("employees.getAttributeClass() = " + employees.getAttributeClass());
        System.out.println("employees.getAttribute().getOriginalClass() = " + employees.getAttribute().getOriginalClass());


    }





}
