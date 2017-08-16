package dk.lessismore.nojpa.db.simplemodel;

import dk.lessismore.nojpa.cache.ObjectCacheFactory;
import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.reflection.db.DatabaseCreator;
import dk.lessismore.nojpa.reflection.db.DbObjectVisitor;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created : with IntelliJ IDEA.
 * User: seb
 */
public class SimpleModelTest {
    private static void save(ModelObjectInterface o){
        ModelObjectService.save(o);
    }

    private final Logger log = LoggerFactory.getLogger(getClass());


    @Test
    public void simpleTest() throws Exception {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.simplemodel");

        Phone phone = ModelObjectService.create(Phone.class);
        phone.setDescription("Some other name");
        phone.setLongDescription("Some long text");
        save(phone);

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
    public void visitorTest() throws Exception {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.simplemodel");

        for(int i = 0; i < 50; i++) {
            Phone phone = ModelObjectService.create(Phone.class);
            phone.setDescription(i + ": Some other name");
            phone.setLongDescription(i + "...Some long text");
            save(phone);
        }

        System.out.println("-------------------------------------- clean cache - start");
        ObjectCacheFactory.getInstance().getObjectCache(Phone.class).clear();
        System.out.println("-------------------------------------- clean cache - end");

        MQL.select(Phone.class).visit(new DbObjectVisitor<Phone>() {
            @Override
            public void visit(Phone phone) {
                System.out.println(" = " + phone.getDescription());
            }

            @Override
            public void setDone(boolean b) {

            }

            @Override
            public boolean getDone() {
                return false;
            }
        }, 2);

    }


    @Test
    public void oldTest() throws Exception {
        log.debug("asdasd");
        log.warn("asdasd");
        log.error("asdasd");



    }


}
