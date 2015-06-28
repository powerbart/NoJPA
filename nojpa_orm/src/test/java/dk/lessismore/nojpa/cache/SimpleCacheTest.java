package dk.lessismore.nojpa.cache;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.db.testmodel.Address;
import dk.lessismore.nojpa.db.testmodel.Car;
import dk.lessismore.nojpa.db.testmodel.InitTestDatabase;
import dk.lessismore.nojpa.db.testmodel.Person;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class SimpleCacheTest {


    final private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RemoteCacheTest.class);


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
//    @Test
//    public void testRemoteRef() throws Exception {
//        Car first = MQL.select(Car.class).getFirst();
//
//        String addressObjectID = "" +first.getAddress();
//        log.debug("addressObjectID : " + addressObjectID);
//        Car mock = MQL.mock(Car.class);
//        log.debug("MQL.selectByID(Address.class, addressObjectID); ------ START");
//        MQL.selectByID(Address.class, addressObjectID);
//        log.debug("MQL.selectByID(Address.class, addressObjectID); ------ END");
//        log.debug("----------- MQL.testRemoteRef : 1");
//        MQL.select(mock).where(mock.getAddress(), MQL.Comp.EQUAL, first.getAddress()).orderBy(mock.getCreationDate(), MQL.Order.DESC).getFirstFromRefAttribute();
//        log.debug("----------- MQL.testRemoteRef : 2");
//        MQL.select(mock).where(mock.getAddress(), MQL.Comp.EQUAL, first.getAddress()).orderBy(mock.getCreationDate(), MQL.Order.DESC).getFirstFromRefAttribute();
//        log.debug("----------- MQL.testRemoteRef : 3");
//
//
//        Assert.assertEquals(true, true);
//    }


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



}
