package dk.lessismore.nojpa.db.simplemodel;

import dk.lessismore.nojpa.cache.ObjectCacheFactory;
import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.reflection.db.DatabaseCreator;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import org.junit.Test;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class SimpleModelTest {


    @Test
    public void simpleTest() throws Exception {
        DatabaseCreator.createDatabase("dk.lessismore.reusable_v4.db.simplemodel");

        Phone phone = ModelObjectService.create(Phone.class);
        phone.setDescription("Some other name");
        phone.setLongDescription("Some long text");
        ModelObjectService.save(phone);

        System.out.println("-------------------------------------- clean cache - start");
        ObjectCacheFactory.getInstance().getObjectCache(Phone.class).clear();
        System.out.println("-------------------------------------- clean cache - end");

        Phone mock = MQL.mock(Phone.class);
        List<Phone> list = MQL.select(mock).getList();
        for(int i = 0; i < list.size(); i++){
            Phone phone1 = list.get(i);
            System.out.println("phone1.getLongDescription() = " + phone1.getLongDescription());
        }


    }
    

    @Test
    public void oldTest() throws Exception {




    }


}
