package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.cache.ObjectCacheFactory;
import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.reflection.db.DatabaseCreator;

import dk.lessismore.nojpa.reflection.db.DbClassReflector;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttribute;
import dk.lessismore.nojpa.reflection.db.attributes.DbAttributeContainer;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created : by IntelliJ IDEA.
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
    public void testDbCreateInline() throws Exception {
        Class<Address> addressClass = Address.class;
        List<Class> l = new ArrayList<Class>();
        l.add(addressClass);
        DatabaseCreator.createDatabase(l);

        Address address = ModelObjectService.create(Address.class);
        address.setArea("MyArea");
        {
            Phone phone = ModelObjectService.create(Phone.class);
            phone.setFunnyD(232d);
            phone.setNumber("MyNumberRocks!");
            address.setA(phone);
        }
        {
            Phone phone = ModelObjectService.create(Phone.class);
            phone.setFunnyD(123d);
            phone.setNumber("B-For-Big!");
            address.setB(phone);
        }
        ModelObjectService.save(address);
//        {
//            ObjectCacheFactory.getInstance().getObjectCache(Address.class).clear();
//            ObjectCacheFactory.getInstance().getObjectCache(Phone.class).clear();
//            Address mock = MQL.mock(Address.class);
//            List<Address> myArea = MQL.select(mock).where(mock.getArea(), MQL.Comp.EQUAL, "MyArea").getList();
//            System.out.println(myArea.size());
//        }

        {
            ObjectCacheFactory.getInstance().getObjectCache(Address.class).clear();
            ObjectCacheFactory.getInstance().getObjectCache(Phone.class).clear();
            Address mock = MQL.mock(Address.class);
            List<Address> myArea = MQL.select(mock).where(mock.getA().getNumber(), MQL.Comp.EQUAL, "MyNumberRocks!").getList();
            System.out.println(myArea.size());
        }

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
