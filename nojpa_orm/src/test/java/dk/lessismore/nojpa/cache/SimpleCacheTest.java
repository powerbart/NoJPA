package dk.lessismore.nojpa.cache;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.db.testmodel.Address;
import dk.lessismore.nojpa.db.testmodel.Car;
import dk.lessismore.nojpa.db.testmodel.InitTestDatabase;
import dk.lessismore.nojpa.db.testmodel.Person;
import dk.lessismore.nojpa.utils.TimerWithPrinter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class SimpleCacheTest {

    private static final Logger log = LoggerFactory.getLogger(SimpleCacheTest.class);

    @BeforeClass
    public static void initDatabase() {
        InitTestDatabase.initPlanetExpress();
        InitTestDatabase.createTestData();
    }


    @Test
    public void testFirstUnique() throws Exception {
        Car first = MQL.select(Car.class).getFirst();

        String addressObjectID = "" +first.getAddress();
        log.debug("addressObjectID : " + addressObjectID);
        Car mock = MQL.mock(Car.class);
        log.debug("MQL.selectByID(Address.class, addressObjectID); ------ START");
        MQL.selectByID(Address.class, addressObjectID);
        log.debug("MQL.selectByID(Address.class, addressObjectID); ------ END");
        log.debug("----------- MQL.selectByFirstUnique : 1");
        MQL.selectByFirstUnique(mock, MQL.has(mock.getAddress(), MQL.Comp.EQUAL, first.getAddress()));
        log.debug("----------- MQL.selectByFirstUnique : 2");
        MQL.selectByFirstUnique(mock, MQL.has(mock.getAddress(), MQL.Comp.EQUAL, first.getAddress()));
        log.debug("----------- MQL.selectByFirstUnique : 3");


        Assert.assertEquals(true, true);
    }


    //MQL.select(mRunner).where(mRunner.getArticle(), MQL.Comp.EQUAL, article).orderBy(mRunner.getCreationDate(), MQL.Order.DESC).getFirst();
    @Test
    public void testRemoteRef() throws Exception {
        Car first = MQL.select(Car.class).getFirst();

        String addressObjectID = "" +first.getAddress();
        log.debug("addressObjectID : " + addressObjectID);
        Car mock = MQL.mock(Car.class);
        log.debug("MQL.selectByID(Address.class, addressObjectID); ------ START");
        MQL.selectByID(Address.class, addressObjectID);
        log.debug("MQL.selectByID(Address.class, addressObjectID); ------ END");
        log.debug("----------- MQL.testRemoteRef : 1");
        MQL.select(mock).where(mock.getAddress(), MQL.Comp.EQUAL, first.getAddress()).orderBy(mock.getCreationDate(), MQL.Order.DESC).getList();
        log.debug("----------- MQL.testRemoteRef : 2");
        MQL.select(mock).where(mock.getAddress(), MQL.Comp.EQUAL, first.getAddress()).orderBy(mock.getCreationDate(), MQL.Order.DESC).getList();
        log.debug("----------- MQL.testRemoteRef : 3");


        Assert.assertEquals(true, true);
    }


    @Test
    public void testSimple() throws Exception {
        Person first = MQL.select(Person.class).getFirst();


        log.debug("----------- MQL.testSimple : 1");
        first.getAddresses();
        log.debug("----------- MQL.testSimple : 2");
        first.getAddresses();
        log.debug("----------- MQL.testSimple : 3");
        ObjectCacheFactory.getInstance().getObjectCache(first).removeFromCache("" + first);
        first.getAddresses();
        log.debug("----------- MQL.testSimple : 4");


        Assert.assertEquals(true, true);
    }


    @Test
    public void testNotNull() throws Exception {
        String first = "" + MQL.select(Person.class).getFirst();
        TimerWithPrinter timer = new TimerWithPrinter("testNotNull", "deleteMe");
        for(int i = 0; i < 1000; i++) {
            timer.markLap("A-1");
            {
                log.debug("----------- MQL.testSimple : 1 - SHOULD NOT GO TO DATABASE");
                Person person = MQL.selectByID(Person.class, first);
                Person mock = MQL.mock(Person.class);
                boolean aNull = MQL.isNull(person, mock, mock.getAddresses());
                log.debug("----------- MQL.testSimple : 2 aNull(" + aNull + ")");
            }
            timer.markLap("A-2");
            timer.markLap("CACHE-1");
            ObjectCacheFactory.getInstance().getObjectCache(Person.class).clear();
            ObjectCacheFactory.getInstance().getObjectCache(Address.class).clear();
            timer.markLap("CACHE-2");
            timer.markLap("B-1");
            {
                log.debug("----------- MQL.testSimple : 1 - SHOULD GO TO DATABASE");
                Person person = MQL.selectByID(Person.class, first);
                timer.markLap("B-2");
                Person mock = MQL.mock(Person.class);
                timer.markLap("B-3");
                boolean aNull = person.getAddresses() != null;
                log.debug("----------- MQL.testSimple : 2 aNull(" + aNull + ")");
            }
            timer.markLap("B-4");
            timer.markLap("W-1");
            wait1sec();
            timer.markLap("W-2");
            if(i % 10 == 0) TimerWithPrinter.printLapsInAlfaOrder();
        }

        Assert.assertEquals(true, true);
    }


    @Test
    public void testNotNull2() throws Exception {
        String first = "" + MQL.select(Person.class).getFirst();
        log.debug("----------- MQL.testSimple : 1 - SHOULD NOT GO TO DATABASE");
        ObjectCacheFactory.getInstance().getObjectCache(Person.class).clear();
        ObjectCacheFactory.getInstance().getObjectCache(Address.class).clear();
        Person person = MQL.selectByID(Person.class, first);
        Person mock = MQL.mock(Person.class);
        boolean aNull = MQL.isNull(person, mock, mock.getAddresses());
        log.debug("----------- MQL.testSimple : 3 aNull(" + aNull + ")");
        boolean bNull = person.getAddresses() == null;
        log.debug("----------- MQL.testSimple : 3 aNull(" + bNull + ")");
        Assert.assertEquals(true, true);
    }


    private static void wait1sec(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}
