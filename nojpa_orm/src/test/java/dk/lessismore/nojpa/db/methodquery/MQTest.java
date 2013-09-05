package dk.lessismore.nojpa.db.methodquery;

import dk.lessismore.nojpa.db.statements.ContainerExpression;
import dk.lessismore.nojpa.db.statements.SQLStatementFactory;
import dk.lessismore.nojpa.db.statements.SelectSQLStatement;
import dk.lessismore.nojpa.db.statements.WhereSQLStatement;
import dk.lessismore.nojpa.reflection.db.DbObjectSelector;
import dk.lessismore.nojpa.reflection.db.statements.SelectSqlStatementCreator;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

import dk.lessismore.nojpa.db.testmodel.*;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.DbObjectVisitor;
import dk.lessismore.nojpa.cache.ObjectCacheFactory;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;


public class MQTest {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MQTest.class);

    //TODO:
    //METHOD: @PreInterceptor
    //METHOD: @PostInterceptor
    //METHOD: @OverrideInterceptor
    //METHOD: @Transient

    //new variable: List, Collection, Set, long, double, bigInterger,
    //Exceptions from DB, should be throw
    //ModelObjectService should be protected
    //Lock( <? extend ModelObject> )
    //CLASS: @HistoryEnable
    //METHOD:
    //InitDatabaseServiceImpl .... Person.class, new Index(person.getName(), person.getAddress().getCity()), new Index(person.getName())
    //Implement MQ.select(X.class).where(MQ.or(person.getName(), MQ.Comp.EQUAL, "seb"),  MQ.or(person.getName(), MQ.Comp.EQUAL, "sebastian"))
    //                     .where(MQ.or(person.getName(), MQ.Comp.EQUAL, "seb"),  MQ.or(person.getName(), MQ.Comp.EQUAL, "sebastian"),  MQ.or(person.getName(), MQ.Comp.EQUAL, "sebs"))
   // Multiple VM's :-)
    // .getArray( new ArrayList<String>(), person.getName() )
    // .getList(  )
    // .getVisit(new MyPersonVisitor);
    // check at  MQ.select(mock).where(mock.getChannel().getObjectID(), MQ.Comp.EQUAL, channelID) ikke laver join ....
    // Verify that the database contains all right columns, etc. 





//    @Test
//    public void testOrs1() throws Exception {
//        {
//            InitTestDatabase.initPlanetExpress();
//            Person p = ModelObjectService.create( Person.class );
//            String s = "asdada";
//            p.setName( s );
//            save( p );
//        }
//        {
//            Person pMock = MQ.mock(Person.class);
//            MQ.SelectQuery<Person> query =
//                    MQ.select(pMock)
//                            .where(pMock.getIsSick(), MQ.Comp.EQUAL, true)
//                            .where(pMock.getName(), MQ.Comp.EQUAL, "MyFirst")
//                            .OR()
//                            .where(pMock.getIsSick(), MQ.Comp.EQUAL, false)
//                            .where(pMock.getName(), MQ.Comp.EQUAL, "MySec-false")
//            ;
//            System.out.println("MQ.select(pMock)...getCount() = " + query.getCount());
//        }
//    }
//
//    @Test
//    public void testOrs2() throws Exception {
//        {
//            InitTestDatabase.initPlanetExpress();
//            Person p = ModelObjectService.create( Person.class );
//            String s = "asdada";
//            p.setName( s );
//            save( p );
//        }
//        {
//            Person pMock = MQ.mock(Person.class);
//            MQ.SelectQuery<Person> query =
//                    MQ.select(pMock)
//                            .where(pMock.getIsSick(), MQ.Comp.EQUAL, true)
//                            .OR()
//                            .BRACKET_START()
//                            .where(pMock.getName(), MQ.Comp.EQUAL, "MyFirst")
//                            .OR()
//                            .where(pMock.getIsSick(), MQ.Comp.EQUAL, false)
//                            .OR()
//                            .where(pMock.getName(), MQ.Comp.EQUAL, "MySec-false")
//                            .BRACKET_END()
//                            .AND()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr")
//            ;
//            System.out.println("MQ.select(pMock)...getCount() = " + query.getCount());
//        }
//    }
//    @Test
//
//    public void testOrs2_1() throws Exception {
//        {
//            InitTestDatabase.initPlanetExpress();
//            Person p = ModelObjectService.create( Person.class );
//            String s = "asdada";
//            p.setName( s );
//            save( p );
//        }
//        {
//            Person pMock = MQ.mock(Person.class);
//            MQ.SelectQuery<Person> query =
//                    MQ.select(pMock)
//                            .where(pMock.getIsSick(), MQ.Comp.EQUAL, true)
//                            .AND()
//                            .BRACKET_START()
//                            .where(pMock.getName(), MQ.Comp.EQUAL, "MyFirst")
//                            .OR()
//                            .where(pMock.getIsSick(), MQ.Comp.EQUAL, false)
//                            .AND()
//                            .where(pMock.getName(), MQ.Comp.EQUAL, "MySec-false")
//                            .BRACKET_END()
//                            .OR()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr")
//            ;
//            System.out.println("MQ.select(pMock)...getCount() = " + query.getCount());
//        }
//    }
//
//    @Test
//    public void testOrs3() throws Exception {
//        {
//            InitTestDatabase.initPlanetExpress();
//            Person p = ModelObjectService.create( Person.class );
//            String s = "asdada";
//            p.setName( s );
//            save( p );
//        }
//        {
//            Person pMock = MQ.mock(Person.class);
//            MQ.SelectQuery<Person> query =
//                    MQ.select(pMock)
//                            .where(pMock.getIsSick(), MQ.Comp.EQUAL, true)
//                            .OR()
//                            .BRACKET_START()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr")
//                            .BRACKET_END()
//            ;
//            System.out.println("MQ.select(pMock)...getCount() = " + query.getCount());
//        }
//    }
//
//    @Test
//    public void testOrs4() throws Exception {
//        {
//            InitTestDatabase.initPlanetExpress();
//            Person p = ModelObjectService.create( Person.class );
//            String s = "asdada";
//            p.setName( s );
//            save( p );
//        }
//        {
//            Person pMock = MQ.mock(Person.class);
//            MQ.SelectQuery<Person> query =
//                    MQ.select(pMock)
//                            .where(pMock.getIsSick(), MQ.Comp.EQUAL, true)
//                            .AND()
//                            .BRACKET_START()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr")
//                            .OR()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr2")
//                            .BRACKET_END()
//            ;
//            System.out.println("MQ.select(pMock)...getCount() = " + query.getCount());
//        }
//    }
//
//    @Test
//    public void testOrs5() throws Exception {
//        {
//            InitTestDatabase.initPlanetExpress();
//            Person p = ModelObjectService.create( Person.class );
//            String s = "asdada";
//            p.setName( s );
//            save( p );
//        }
//        {
//            Person pMock = MQ.mock(Person.class);
//            MQ.SelectQuery<Person> query =
//                    MQ.select(pMock)
//                            .where(pMock.getIsSick(), MQ.Comp.EQUAL, true)
//                            .OR()
//                            .BRACKET_START()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr")
//                            .OR()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr2")
//                            .BRACKET_END()
//                            .AND()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescrAND")
//
//            ;
//            System.out.println("MQ.select(pMock)...getCount() = " + query.getCount());
//        }
//    }
//
//    @Test
//    public void testOrs5_1() throws Exception {
//        {
//            InitTestDatabase.initPlanetExpress();
//            Person p = ModelObjectService.create( Person.class );
//            String s = "asdada";
//            p.setName( s );
//            save( p );
//        }
//        {
//            Person pMock = MQ.mock(Person.class);
//            MQ.SelectQuery<Person> query =
//                    MQ.select(pMock)
//                            .where(pMock.getIsSick(), MQ.Comp.EQUAL, true)
//                            .AND()
//                            .BRACKET_START()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr")
//                            .AND()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr2")
//                            .BRACKET_END()
//                            .OR()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescrAND")
//
//            ;
//            System.out.println("MQ.select(pMock)...getCount() = " + query.getCount());
//        }
//    }
//
//    @Test
//    public void testOrs5_2() throws Exception {
//        Address a1 = ModelObjectService.create( Address.class );
//        Address a2 = ModelObjectService.create( Address.class );
//        {
//            InitTestDatabase.initPlanetExpress();
//            {
//                Person p = ModelObjectService.create( Person.class );
//                String s = "asdada";
//                p.setName( s );
//                save( p );
//            }
//            {
//
//                a1.setStreet("Bellagade 111");
//                save( a1 );
//
//                a2.setStreet("Bellagade 112");
//                save( a2 );
//
//            }
//        }
//        {
//            Person pMock = MQ.mock(Person.class);
//            MQ.SelectQuery<Person> query =
//                    MQ.select(pMock)
//                            .BRACKET_START()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr")
//                            .OR()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr2")
//                            .BRACKET_END()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescrAND2")
//                            .whereIn(pMock.getAddresses()[MQ.ANY], new Address[]{ a1, a2 })
//
//            ;
//            System.out.println("MQ.select(pMock)...getCount() = " + query.getCount());
//        }
//    }
//
//
//    //This is the one not working ....!
//    @Test
//    public void testOrs5_3() throws Exception {
//        Address a1 = ModelObjectService.create( Address.class );
//        Address a2 = ModelObjectService.create( Address.class );
//        {
//            InitTestDatabase.initPlanetExpress();
//            {
//                Person p = ModelObjectService.create( Person.class );
//                String s = "asdada";
//                p.setName( s );
//                save( p );
//            }
//            {
//
//                a1.setStreet("Bellagade 111");
//                save( a1 );
//
//                a2.setStreet("Bellagade 112");
//                save( a2 );
//
//            }
//        }
//        {
//            Person pMock = MQ.mock(Person.class);
//            MQ.SelectQuery<Person> query =
//                    MQ.select(pMock)
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr")
//                            .AND()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr2")
//                            .AND()
//                            .whereIn(pMock.getAddresses()[MQ.ANY], new Address[]{ a1, a2 })
//
//            ;
//            System.out.println("MQ.select(pMock)...getCount() = " + query.getCount());
//        }
//    }
//
//
//
//
//    @Test
//    public void testOrs5_4() throws Exception {
//        Address a1 = ModelObjectService.create( Address.class );
//        Address a2 = ModelObjectService.create( Address.class );
//        {
//            InitTestDatabase.initPlanetExpress();
//            {
//                Person p = ModelObjectService.create( Person.class );
//                String s = "asdada";
//                p.setName( s );
//                save( p );
//            }
//            {
//
//                a1.setStreet("Bellagade 111");
//                save( a1 );
//
//                a2.setStreet("Bellagade 112");
//                save( a2 );
//
//            }
//        }
//        {
//            Person pMock = MQ.mock(Person.class);
//            MQ.SelectQuery<Person> query =
//                    MQ.select(pMock)
////                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr")
////                            .AND()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr2")
//                            .AND()
//                            .whereIn(pMock.getAddresses()[MQ.ANY], new Address[]{ a1, a2 })
//
//            ;
//            System.out.println("MQ.select(pMock)...getCount() = " + query.getCount());
//        }
//    }
//
//
//
//    @Test
//    public void testOrs5_5() throws Exception {
//        Address a1 = ModelObjectService.create( Address.class );
//        Address a2 = ModelObjectService.create( Address.class );
//        Address a3 = ModelObjectService.create( Address.class );
//        {
//            InitTestDatabase.initPlanetExpress();
//            {
//                Person p = ModelObjectService.create( Person.class );
//                String s = "asdada";
//                p.setName( s );
//                save( p );
//            }
//            {
//
//                a1.setStreet("Bellagade 111");
//                save( a1 );
//
//                a2.setStreet("Bellagade 112");
//                save( a2 );
//
//                a3.setStreet("Bellagade 333");
//                save( a3 );
//
//            }
//        }
//        {
//            Person pMock = MQ.mock(Person.class);
//            MQ.SelectQuery<Person> query =
//                    MQ.select(pMock)
////                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr")
////                            .AND()
//                            .where(pMock.getDescription(), MQ.Comp.LIKE, "SomeDescr2")
//                            .whereIn(pMock.getAddresses()[MQ.ANY], new Address[]{ a1, a2 })
//                            .whereIn(pMock.getAddresses()[MQ.ANY], new Address[]{ a2, a3 })
//
//            ;
//            System.out.println("MQ.select(pMock)...getCount() = " + query.getCount());
//        }
//    }


//    AccountLine mockAccountLine = MQ.mock(AccountLine.class);
//    long sum = MQ.select(mockAccountLine).where(mockAccountLine.getAccount(), MQ.Comp.EQUAL, me).getSum(mockAccountLine.getAmount().getCents());


    @Test
    public void testGetByID() throws Exception {
        {
            InitTestDatabase.initPlanetExpress();
            for(int i = 0; i < 30; i++){
                Person p = ModelObjectService.create(Person.class);
                p.setName("Tester" + i);
                save( p );
            }


            for(int i = 0; i< 10; i++){
                final int it = i;
                Thread t = new Thread(){
                    public void run(){
                        Person p = MQL.select(Person.class).limit(it, it+1).getFirst();

                        for(int i = 0; i < 100; i++){
//                            Person person = MQ.selectByID(Person.class, p.getObjectID());
//                            save(person);

                            Company mock = MQL.mock(Company.class);
                            long sum = MQL.select(mock).where(mock.getCfo().getName(), MQL.Comp.LIKE, "%e%").getSum(mock.getCeo().getCountOfCars());


                            Person p2 = MQL.selectByID(Person.class, p.getObjectID());
                            log.debug("person = " + p2.getName());
//                            Person[] array = MQ.select(Person.class).getArray();
//                            log.debug("persons.size() " + array.length);
                        }
                    }
                };
                t.start();
            }
        }
        Thread.sleep(1000 * 30);
        assertEquals(true, true);
    }
    private static void save(ModelObjectInterface o){
        ModelObjectService.save(o);
    }



    @Test
    public void testNull() throws Exception {
        // TODO fun fun fun
        InitTestDatabase.initPlanetExpress();
        Person p = ModelObjectService.create( Person.class );
        p.setName("null");
        ModelObjectService.save(p);
        Person person = MQL.mock(Person.class);
        MQL.SelectQuery<Person> aNull1 = MQL.select(person).where(person.getName(), MQL.Comp.EQUAL, "null");
        Person first = aNull1.getFirst();
//        System.out.println("first.getName() = " + first.getName());
    }


    @Test
    public void testGets() throws Exception {
        {
            InitTestDatabase.initPlanetExpress();
            Person p = ModelObjectService.create( Person.class );
            p.setName( "MyName" );
            p.setSomeFloat(0.99999f);
            p.setCountOfFriends(Long.MAX_VALUE);
            Cpr c = ModelObjectService.create( Cpr.class );
            String number = "12345678";
            c.setNumber(number);
            p.setCpr(c);
            save( c );
            save( p );
            System.out.println("p.getCpr().getNumber() = " + p.getCpr().getNumber());
            ObjectCacheFactory.getInstance().getObjectCache(Person.class).clear();
            Person mock = MQL.mock(Person.class);
            Person person = MQL.select(mock).where(mock.getCountOfFriends(), MQL.Comp.EQUAL_OR_GREATER, 8L).getFirst();
            System.out.println("person = " + person);
            System.out.println("person = " + person.getCountOfFriends());
            System.out.println("person = " + person.getSomeFloat());

        }
        assertEquals(true, true);
    }

    @Test
    public void testDbAttributeDirect() throws Exception {
        {
            InitTestDatabase.initPlanetExpress();
            Person p = ModelObjectService.create( Person.class );
            p.setName( "MyName" );
            Cpr c = ModelObjectService.create( Cpr.class );
            c.setNumber("12345678");
            p.setCpr(c);
            save( c );
            save( p );
        }
        SelectSqlStatementCreator creator = new SelectSqlStatementCreator();
        creator.setSource(Person.class);
        ContainerExpression selectExpression = SQLStatementFactory.getContainerExpression();
        selectExpression.addExpression(WhereSQLStatement.OR, SQLStatementFactory.getLeafExpression().addConstrain("name", WhereSQLStatement.LIKE, "My%"));
        selectExpression.addExpression(WhereSQLStatement.OR, SQLStatementFactory.getLeafExpression().addConstrain("name", WhereSQLStatement.LIKE, "Tis%"));
        creator.getSelectSQLStatement().addExpression(selectExpression);
        creator.getSelectSQLStatement().setOrderBy("name", SelectSQLStatement.ASC);

        SelectSQLStatement selectSQLStatement = creator.getSelectSQLStatement();
        List<Person> ps = DbObjectSelector.selectObjectsFromDb(Person.class, selectSQLStatement);
        System.out.println("ps.length = " + ps.size());
        for(int i = 0; i < ps.size(); i++){
            System.out.println(ps.get(i).getName());
        }


        assertEquals(true, true);
    }

    @Test
    public void testOrderBy() throws Exception {
        {
            InitTestDatabase.initPlanetExpress();
            Person person = MQL.mock(Person.class);
            List<Person> list = MQL.select(person).where(person.getCpr().getNumber(), MQL.Comp.EQUAL, "123").orderBy(person.getName(), MQL.Order.DESC).orderBy(person.getCreationDate(), MQL.Order.DESC).getList();

            MQL.select(person).orderBy(person.getName(), MQL.Order.DESC).orderBy(person.getCreationDate(), MQL.Order.DESC).getList();
        }
        assertEquals(true, true);
    }

    @Test
    public void testGetFirst() throws Exception {
        {
            InitTestDatabase.initPlanetExpress();
            Person person = MQL.mock(Person.class);
            Person first = MQL.select(person).orderBy(person.getName(), MQL.Order.DESC).orderBy(person.getCreationDate(), MQL.Order.DESC).limit(10, 12).getFirst();
        }
        assertEquals(true, true);
    }


    @Test
    public void testWhereWasNotNulls() throws Exception {
        {
            InitTestDatabase.initPlanetExpress();
            {
                Person pp = ModelObjectService.create(Person.class);
                pp.setDescription("pppp2");
                save( pp );
            }
            Person pp = ModelObjectService.create(Person.class);
            pp.setDescription("pppp");
            Car c = ModelObjectService.create(Car.class);
            save( c );
            pp.setCar(c);
            save( pp );

            pp.setCar(c);
            save( pp );

            Person mPerson = MQL.mock(Person.class);

            MQL.select(mPerson).where(mPerson.getCountOfCars(), MQL.Comp.GREATER, 1).whereIsNotNull(mPerson.getCreationDate()).whereIsNull(mPerson.getCreationDate()).where(mPerson.getDescription(), MQL.Comp.LIKE, "%descr%").orderBy(mPerson.getName(), MQL.Order.DESC).orderBy(mPerson.getCreationDate(), MQL.Order.DESC).limit(10, 12).getFirst();
        }
        assertEquals(true, true);
    }

    @Test
    public void testProxyCache_1() throws Exception {
        InitTestDatabase.initPlanetExpress();
        Person p = ModelObjectService.create(Person.class);
        System.out.println("p = " + p + " / " + p.getClass());
        ObjectCacheFactory.getInstance().getObjectCache(p).putInCache(p.getObjectID(), (ModelObject) p);
        Object cache = ObjectCacheFactory.getInstance().getObjectCache(p).getFromCache(p.getObjectID());
        System.out.println("cache = " + cache  + " / " + cache.getClass());
        Person p2 = (Person) cache;
    }

    @Test
    public void testWhereUnsafes() throws Exception {
        // TODO seb, how to use where with a second mocked value? e.g.
        // TODO WHERE _Company.number1 > _Company.number2
        // TODO or
        // TODO WHERE _Company.number1 > _JoinedTable.number2

        InitTestDatabase.initPlanetExpress();
        Company mCompany = MQL.mock(Company.class);
        MQL.select(mCompany)
                .whereUnsafe(Company.class, "myInt", MQL.Comp.LESS, "myLong")
                .getCount();
    }





    @Test
    public void testWhereInEnum() throws Exception {
        // COOOL !!!! seb: just check this, because i've implemented the whereIn(mockedEnum, enum[]) and i think it works, but just to be sure
        InitTestDatabase.initPlanetExpress();
        Car mCar = MQL.mock(Car.class);
        MQL.select(mCar).whereIn(mCar.getFuelType(), new Car.FuelType[] {Car.FuelType.ELECTRIC, Car.FuelType.DIESEL}).getFirst();
    }


    @Test
    public void testHasIn() throws Exception {
        InitTestDatabase.initPlanetExpress();
        Person p = ModelObjectService.create(Person.class);
        p.setName("MyName");

        Address[] as = new Address[10];
        for(int i = 0; i < 10; i++){
            Address a = ModelObjectService.create(Address.class);
            a.setStreet("MyStreet" + i);
            save( a );
            as[i] = a;
        }
        p.setAddresses( as );
        save( p );

        Person mPerson = MQL.mock(Person.class);
        Address[] addresses = Arrays.copyOfRange(as, 2, 5);

        // FIXED: gives strange amount of joins where clauses !!!
        MQL.select(mPerson).where(MQL.all(MQL.hasIn(mPerson.getName(), "MyName", "m1", "m2", "m3", "m4"))).getFirst();

    }



    @Test
    public void testWhereInArray() throws Exception {
        InitTestDatabase.initPlanetExpress();
        Person p = ModelObjectService.create(Person.class);
        p.setName("MyName");

        Address[] as = new Address[10];
        for(int i = 0; i < 10; i++){
            Address a = ModelObjectService.create(Address.class);
            a.setStreet("MyStreet" + i);
            save( a );
            as[i] = a;
        }
        p.setAddresses( as );
        save( p );

        Person mPerson = MQL.mock(Person.class);
        Address[] addresses = Arrays.copyOfRange(as, 2, 5);

        // FIXED: gives strange amount of joins where clauses !!!
        MQL.select(mPerson).whereIn(mPerson.getAddresses()[MQL.ANY], addresses).getArray();

    }

    @Test
    public void testCache_1() throws Exception {
        InitTestDatabase.initPlanetExpress();
        Person p = ModelObjectService.create(Person.class);
        p.setName("MyName");

        Car car = ModelObjectService.create(Car.class);
        car.setBrand("Volvo");
        car.setVolume(2f);
        p.setCar( car );

        save( p );

        System.out.println("MQ.select( Car.class ).getCount() = " + MQL.select(Car.class).getCount());
        System.out.println("MQ.select( Person.class ).getCount() = " + MQL.select(Person.class).getCount());

        MQL.SelectQuery<Person> query = MQL.select(Person.class);
        Person first = query.getFirst();
        System.out.println("MQ.select(Person.class).getFirst().getCar() = " + first.getCar());
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("MQ.select(Person.class).getFirst().getCar() = " + MQL.select(Person.class).getFirst().getCar());
    }

    @Test
    public void testCache_3() throws Exception {
        InitTestDatabase.initPlanetExpress();
        Person p = ModelObjectService.create(Person.class);
        p.setName("MyName");

        Address[] as = new Address[10];
        for(int i = 0; i < 10; i++){
            Address a = ModelObjectService.create(Address.class);
            a.setStreet("MyStreet" + i);
            save( a );
            as[i] = a;
        }
        p.setAddresses( as );
        save( p );


        System.out.println("MQ.select( Car.class ).getCount() = " + MQL.select(Address.class).getCount());
        System.out.println("MQ.select( Person.class ).getCount() = " + MQL.select(Person.class).getCount());

        System.out.println("MQ.select(Person.class).getFirst().getCar() = " + MQL.select(Person.class).getFirst().getAddresses());
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("MQ.select(Person.class).getFirst().getCar() = " + MQL.select(Person.class).getFirst().getAddresses());
    }


    @Test
    public void testCache_2() throws Exception {
        InitTestDatabase.initPlanetExpress();
        Person p = ModelObjectService.create(Person.class);
        p.setName("MyName");
        System.out.println("testCache_2() : " + p + " / " + p.getClass());
        save( p );

        System.out.println("MQ.select( Person.class ).getCount() = " + MQL.select(Person.class).getCount());

        MQL.SelectQuery<Person> query = MQL.select(Person.class);
        Person first = query.getFirst();
        System.out.println("MQ.select(Person.class).getFirst().getCar() = " + first.getCar());
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("MQ.select(Person.class).getFirst().getCar() = " + MQL.select(Person.class).getFirst().getCar());



    }

    @Test
    public void testObjEquals() throws Exception {
        Person person = ModelObjectService.create(Person.class);
        save(person);

        Person objID1 = MQL.selectByID(Person.class, person.getObjectID());
        Person objIDother = MQL.selectByID(Person.class, person.getObjectID());


        String equalsTestName = "equalsTestName";
        person.setName(equalsTestName);
        // test if person, objID1 & objIDother are the same

        Assert.assertEquals(equalsTestName.toLowerCase(), objID1.getName().toLowerCase());
        Assert.assertEquals(equalsTestName.toLowerCase(), objIDother.getName().toLowerCase());
    }

    static class TestVisitor implements DbObjectVisitor {


        @Override
        public void visit(ModelObjectInterface m) {
            System.out.println("No visiting " + m);
        }

        @Override
        public void setDone(boolean b) {

        }

        @Override
        public boolean getDone() {
            return false;
        }
    }


    @Test
    public void testVisitor() throws Exception {

        InitTestDatabase.initPlanetExpress();
        for(int i = 0; i < 10; i++){
            Car car = ModelObjectService.create(Car.class);
            car.setBrand("MyBrand" + i);
            car.setVolume(i + (((float) i) / 10f));
            save( car );
        }
        Car car = MQL.mock(Car.class);

        MQL.select(Car.class).where(car.getBrand(), MQL.Comp.LIKE, "MyBrand0").visit(new TestVisitor());

    }



    @Test
    public void testSum_1() throws Exception {
        InitTestDatabase.initPlanetExpress();
        for(int i = 0; i < 10; i++){
            Car car = ModelObjectService.create(Car.class);
            car.setBrand("MyBrand" + i);
            car.setVolume(i + (((float) i) / 10f));
            save( car );
        }
        Car car = MQL.mock(Car.class);
        System.out.println("MQ.select( Car.class ).getCount() = " + MQL.select(Car.class).getList());
        System.out.println("MQ.select( Car.class ).getCount() = " + MQL.select(car).getSum( car.getVolume() ));
    }

    @Test
    public void testSum_2() throws Exception {
        InitTestDatabase.initPlanetExpress();
        for(int i = 0; i < 10; i++){
            Car car = ModelObjectService.create(Car.class);
            car.setBrand("MyBrand" + i);
            car.setVolume(i + (((float) i) / 10f));
            save( car );
        }
        Car car = MQL.mock(Car.class);
        System.out.println("MQ.select( Car.class ).getCount() = " + MQL.select(car).where(car.getBrand(), MQL.Comp.LIKE, "MyBrahh%").getSum( car.getVolume() ));
    }

    @Test
    public void testOr_1() throws Exception {
        InitTestDatabase.initPlanetExpress();
        String[] names = new String[]{"Sebastian", "Thomas", "Mikkel"};
        for(int i = 0; i < names.length; i++){
            Person person = ModelObjectService.create(Person.class);
            person.setName(names[i]);
            save(person);
        }
        Person personMock = MQL.mock(Person.class);

        List<Person> personList = MQL.select(personMock).where(MQL.any(MQL.has(personMock.getName(), MQL.Comp.EQUAL, "Sebastian"), MQL.has(personMock.getName(), MQL.Comp.EQUAL, "Thomas")) ).getList();
        for(int i = 0; i < personList.size(); i++){
            System.out.println("personList.get(i).getName() = " + personList.get(i).getName());
        }
    }



    @Test
    public void testWithIn() throws Exception {
        InitTestDatabase.initPlanetExpress();
        String[] names = new String[]{"Sebastian", "Thomas", "Mikkel"};
        for(int i = 0; i < names.length; i++){
            Person person = ModelObjectService.create(Person.class);
            person.setName(names[i]);
            save(person);
        }
        Person personMock = MQL.mock(Person.class);
        List<Person> personList = MQL.select(personMock).whereIn(personMock.getName(), names).getList();
        for(int i = 0; i < personList.size(); i++){
            System.out.println("personList.get(i).getName() = " + personList.get(i).getName());
        }
    }

    @Test
    public void testWithInWithModelObjectInterfaces() throws Exception {
        InitTestDatabase.initPlanetExpress();
        String[] names = new String[]{"Sebastian", "Thomas", "Mikkel"};
        ArrayList<Car> cars = new ArrayList<Car>();
        for(int i = 0; i < names.length; i++){
            Person person = ModelObjectService.create(Person.class);
            Car car = ModelObjectService.create(Car.class);
            car.setBrand("MyCarBrand" + i);
            cars.add(car);
            person.setCar(car);
            person.setName(names[i]);
            save(person);
        }
        Person personMock = MQL.mock(Person.class);
        List<Person> personList = MQL.select(personMock).whereIn(personMock.getCar(), cars.toArray(new Car[cars.size()])).getList();
        for(int i = 0; i < personList.size(); i++){
            System.out.println("personList.get(i).getName() = " + personList.get(i).getName());
        }


        Person person = MQL.mock(Person.class);
        Person[] persons = MQL.select(person).where(person.getCar().getBrand(), MQL.Comp.EQUAL, "Toyota").getArray();


    }

    @Test
    public void testWithInWithModelObjectInterfacesArrays() throws Exception {
        InitTestDatabase.initPlanetExpress();
        String[] names = new String[]{"Sebastian", "Thomas", "Mikkel"};
        ArrayList<Car> cars = new ArrayList<Car>();
        ArrayList<Person> ps = new ArrayList<Person>();
        for(int i = 0; i < names.length; i++){
            Person person = ModelObjectService.create(Person.class);
            Car car = ModelObjectService.create(Car.class);
            car.setBrand("MyCarBrand" + i);
            cars.add(car);
            person.setCar(car);
            person.setName(names[i]);
            if(ps.size() > 0){
                person.setChildren(ps.toArray(new Person[ps.size()]));
            }
            save(person);
            ps.add(person);
        }
        Person personMock = MQL.mock(Person.class);
        List<Person> personList = MQL.select(personMock).whereIn(personMock.getChildren()[MQL.ANY], ps.toArray(new Person[ps.size()])).getList();
        for(int i = 0; i < personList.size(); i++){
            System.out.println("personList.get(i).getName() = " + personList.get(i).getName());
        }
    }

    @Test
    public void testDbStrip() throws Exception {
        InitTestDatabase.initPlanetExpress();
        Person p = ModelObjectService.create( Person.class );
        String t = "as'/%/'dassda";
        p.setName( t );
        save( p );
        System.out.println("t = " + t);
    }

    @Test
    public void testListAllNames() throws Exception {
        Person person = MQL.mock(Person.class);
        Person[] persons = MQL.select(person).getArray();
        for(int i = 0; i < persons.length; i++){
            System.out.println("persons[i].getName() = " + persons[i].getName());
        }
    }


    @Test
    public void testBooleans() throws Exception {
        {
            InitTestDatabase.initPlanetExpress();
            Person p = ModelObjectService.create( Person.class );
            String s = "asdada";
//            p.setDescription(s);
            p.setName( s );
            save( p );
        }
        {
            Person pMock = MQL.mock(Person.class);
            MQL.SelectQuery<Person> query = MQL.select(pMock).where(pMock.getIsSick(), MQL.Comp.EQUAL, true);
            System.out.println("MQ.select(pMock).where(pMock.getIsSick(), MQ.Comp.EQUAL, true).getCount() = " + query.getCount());
        }
        {
            Person pMock = MQL.mock(Person.class);
            MQL.SelectQuery<Person> query = MQL.select(pMock).where(pMock.getIsSick(), MQL.Comp.EQUAL, false);
            System.out.println("MQ.select(pMock).where(pMock.getIsSick(), MQ.Comp.EQUAL, true).getCount() = " + query.getCount());
        }
    }

    @Test
    public void testDefaultValue() throws Exception {
        InitTestDatabase.initPlanetExpress();
        Person p = ModelObjectService.create( Person.class );
        System.out.println("p.getName() = " + p.getName());
    }
}