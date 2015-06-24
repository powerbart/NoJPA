package dk.lessismore.nojpa.cache;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.db.testmodel.InitTestDatabase;
import dk.lessismore.nojpa.db.testmodel.Person;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import dk.lessismore.nojpa.utils.MaxSizeArray;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 15/12/12
 */
public class RemoteCacheTest {

    final private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RemoteCacheTest.class);


    @BeforeClass
    public static void initDatabase() {
        InitTestDatabase.initPlanetExpress();
        InitTestDatabase.createTestData();
    }


    @Test
    public void testStartUpOfOne() throws Exception {
        ObjectCacheRemote s1 = new ObjectCacheRemote();
        s1.clusterFilenameForTest = "cluster-serverA";
        s1.contextInitialized(null);
        Thread.sleep(260 * 1000);
        s1.contextDestroyed(null);
        Assert.assertEquals(true, true);
    }


    @Test
    public void testStartUpOfTwo_A() throws Exception {
        ObjectCacheRemote s1 = new ObjectCacheRemote();
        s1.clusterFilenameForTest = "cluster-serverA";
        s1.contextInitialized(null);
        {
            Thread.sleep(10 * 1000);
            List<Person> persons = MQL.select(Person.class).getList();
            for (int i = 0; i < persons.size(); i++) {
                Person person = persons.get(i);
                person.setSomeFloat(i);
                ModelObjectService.save(person);
            }
        }
        {
            Thread.sleep(5 * 1000);
            List<Person> persons = MQL.select(Person.class).getList();
            for (int i = 0; i < persons.size(); i++) {
                Person person = persons.get(i);
                person.setSomeFloat((float) Math.random());
                ModelObjectService.save(person);
            }
        }
        {
            Thread.sleep(5 * 1000);
            List<Person> persons = MQL.select(Person.class).getList();
            for (int i = 0; i < persons.size(); i++) {
                Person person = persons.get(i);
                person.setSomeFloat((float) Math.random());
                ModelObjectService.save(person);
            }
        }
        {
            Thread.sleep(5 * 1000);
            List<Person> persons = MQL.select(Person.class).getList();
            for (int i = 0; i < persons.size(); i++) {
                Person person = persons.get(i);
                person.setSomeFloat((float) Math.random());
                ModelObjectService.save(person);
            }
        }
        Thread.sleep(260 * 1000);
        s1.contextDestroyed(null);
        Assert.assertEquals(true, true);
    }

    @Test
    public void testStartUpOfTwo_B() throws Exception {
        ObjectCacheRemote s1 = new ObjectCacheRemote();
        s1.clusterFilenameForTest = "cluster-serverB";
        s1.contextInitialized(null);
        Thread.sleep(260 * 1000);
        s1.contextDestroyed(null);
        Assert.assertEquals(true, true);
    }


    static long sleep = 1;

    @Test
    public void testGlobalLock_UpOfTwo_A() throws Exception {
        ObjectCacheRemote s1 = new ObjectCacheRemote();
        s1.clusterFilenameForTest = "cluster-serverA";
        s1.contextInitialized(null);
        Thread.sleep(1 * 1000 * 10);
        sleep = 1000;
        {
            for(int t = 0; t < 50; t++){
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<Person> persons = MQL.select(Person.class).getList();
                        for (int i = 0; i < persons.size(); i++) {
                            Person person = persons.get(i);
                            person.setSomeFloat((float) Math.random());
                            try {
                                saveWithLock(person);
                                Thread.sleep(4);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            for(int j = 0; j < 100; j++){
                                ObjectCacheRemote.removeFromRemoteCache(person);
                                try {
                                    Thread.sleep(4);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
                thread.start();
            }
        }
        System.out.println("                                                                                                                                                                                                   NOW SLEEPING ........ START..");
        Thread.sleep(1 * 1000 * 500);
        System.out.println("NOW SLEEPING ........ DONE..");
        s1.contextDestroyed(null);
        Assert.assertEquals(true, true);
    }


    @Test
    public void testGlobalLock_UpOfTwo_B() throws Exception {
        ObjectCacheRemote s1 = new ObjectCacheRemote();
        s1.clusterFilenameForTest = "cluster-serverB";
        s1.contextInitialized(null);
        sleep = 1000;

        {

            for(int u = 0; u < 100; u++){
                List<Person> persons = MQL.select(Person.class).getList();
                for (int i = 0; i < persons.size(); i++) {
//                    System.out.println("NOW SLEEPING ........ START..");
//                Thread.sleep(1 * 1000 * 500);
//                System.out.println("NOW SLEEPING ........ START..");
                    Person person = persons.get(i);
                    person.setSomeFloat((float) Math.random());
                    this.saveWithLock(person);
                    try {
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        System.out.println("NOW SLEEPING ........ START..");
        Thread.sleep(1 * 1000 * 500);
        System.out.println("NOW SLEEPING ........ DONE..");
        s1.contextDestroyed(null);
        Assert.assertEquals(true, true);
    }


    @Test
    public void testArray() throws Exception {
        MaxSizeArray<String> toPost = new MaxSizeArray<String>(200);
        toPost.add("A");
        toPost.add("B");
        toPost.add("C");
        toPost.add("D");

        System.out.println(toPost.pop());
        System.out.println(toPost.pop());
        System.out.println(toPost.pop());
        System.out.println(toPost.pop());


        Assert.assertEquals(true, true);
    }


    public static void saveWithLock(Person p) throws Exception {
        GlobalLockService.getInstance().lockAndRun(p, new GlobalLockService.LockedExecutor<Person>() {

            @Override
            public void execute(Person ms) throws Exception {
                log.debug("::::::::::::: EXECUTE - START ("+ ms +")");
//                Thread.sleep(sleep);
                ModelObjectService.save(ms);
                log.debug("::::::::::::: EXECUTE - END");
            }
        });
    }




    static class MyMessageListener implements MessageListener {

        @Override
        public void gotMessage(String message) {
            System.out.println(" WE GOT MESSAGE !!!! (" + message + ")");
        }
    }

    @Test
    public void testGlobalLock_UpOfTwo_A_message() throws Exception {
        ObjectCacheRemote s1 = new ObjectCacheRemote();
        s1.clusterFilenameForTest = "cluster-serverA";
        s1.contextInitialized(null);
        GlobalLockService.getInstance().addListener(new MyMessageListener());
        Thread.sleep(1 * 1000 * 10);
        sleep = 1000;
        System.out.println("NOW SLEEPING ........ START..");
        Thread.sleep(1 * 1000 * 500);
        System.out.println("NOW SLEEPING ........ DONE..");
        s1.contextDestroyed(null);
        Assert.assertEquals(true, true);
    }


    @Test
    public void testGlobalLock_UpOfTwo_B_message() throws Exception {
        ObjectCacheRemote s1 = new ObjectCacheRemote();
        s1.clusterFilenameForTest = "cluster-serverB";
        s1.contextInitialized(null);
        sleep = 1000;
        Thread.sleep(1 * 1000 * 15);


        System.out.println("Sending message ..... - start");
        GlobalLockService.getInstance().sendMessage("Hello Atanas");
        System.out.println("Sending message ..... - done");

        Thread.sleep(1 * 1000 * 500);
        s1.contextDestroyed(null);
        Assert.assertEquals(true, true);
    }





}
