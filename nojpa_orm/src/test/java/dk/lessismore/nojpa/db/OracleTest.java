package dk.lessismore.nojpa.db;

import dk.lessismore.nojpa.cache.ObjectCacheFactory;
import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.db.methodquery.NList;
import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.db.methodquery.NStats;
import dk.lessismore.nojpa.db.oracle.CcbCapitalCostRow;
import dk.lessismore.nojpa.db.oracle.CrbCapitalCost;
import dk.lessismore.nojpa.db.statements.oracle.OracleDB;
import dk.lessismore.nojpa.db.testmodel.Car;
import dk.lessismore.nojpa.db.testmodel.Person;
import dk.lessismore.nojpa.db.testmodel.PersonStatus;
import dk.lessismore.nojpa.reflection.db.DatabaseCreator;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectSearchService;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import dk.lessismore.nojpa.reflection.db.model.SolrServiceImpl;
import dk.lessismore.nojpa.reflection.translate.LessismoreTranslateServiceImpl;
import dk.lessismore.nojpa.utils.Pair;
import junit.framework.Assert;
import oracle.jdbc.OracleTypes;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.Test;

import java.sql.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class OracleTest {



//    PROCEDURE get_measures (rs                     IN OUT refcur,
//                            p_report_date          IN     DATE,
//                            p_counterpart_su_key   IN     NUMBER,
//                            p_str   IN     VARCHAR2,
//                            p_legal_entity_id      IN     NUMBER);


    @Test
    public void testCallIterator() throws Exception {
        CrbCapitalCost crbCapitalCost = OracleDB.connectInterface(CrbCapitalCost.class);
        Iterator<CcbCapitalCostRow> measures = crbCapitalCost.getMeasures(Calendar.getInstance(), 6666L, "myMsg-desc", 3L);
        while(measures.hasNext()){
            CcbCapitalCostRow capitalCostRow = measures.next();
            System.out.println("capitalCostRow.getAbe() = " + capitalCostRow.getAbe());
            System.out.println("capitalCostRow.getName() = " + capitalCostRow.getName());
            System.out.println("capitalCostRow.getB3() = " + capitalCostRow.getB3());
        }


    }

    @Test
    public void testCallSimpleStoredProcedure() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@//192.168.56.101:1521", "system", "1234");
        CallableStatement stmt = conn.prepareCall("{ call crb_capital_cost_pkg.get_measures(?,?,?,?,?) }");
        int parameterCount = 1;
        stmt.registerOutParameter(parameterCount++, OracleTypes.CURSOR);
        stmt.setTimestamp(parameterCount++, new Timestamp(Calendar.getInstance().getTimeInMillis()));
        stmt.setLong(parameterCount++, 121L);
        stmt.setString(parameterCount++, "Hello world");
        stmt.setLong(parameterCount++, 124L);
        stmt.execute();
        ResultSet rs = (ResultSet) stmt.getObject(1);
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();


        for(int i = 1; i <= columnCount; i++){
            System.out.print(metaData.getColumnName(i) + "("+ metaData.getColumnTypeName(i) +")|");
        }



        while (rs.next()) {
            System.out.println("");
            for(int i = 1; i <= columnCount; i++){
                System.out.print(rs.getString(i) + " | ");
            }
        }
        rs.close();
        stmt.close();




    }






    @Test
    public void test02() {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        Person prev = null;
        for (int i = 0; i < 10; i++) {
            Person person = ModelObjectService.create(Person.class);
            if(i % 2 == 0){
                prev = person;
            } else {
                prev.setGirlFriend(person);
                person.setGirlFriend(prev);
            }
            person.setName("person " + (i % 4));
            person.setIsSick(i % 2 == 0);


            person.setPersonStatus(PersonStatus.BETWEEN_RELATIONS);
//            person.setHistoryStatus(new PersonStatus[]{PersonStatus.BETWEEN_RELATIONS, PersonStatus.SINGLE});
            person.setSomeFloat((float) (20f * Math.random()));
            if (i < 7) {
                Car car = ModelObjectService.create(Car.class);
                person.setCar(car);
            }
            ModelObjectService.save(person);
            System.out.println("---------------- PUT START -----------------------");
            ModelObjectSearchService.put(person);
            System.out.println("---------------- PUT END -----------------------");
        }
        solrService.commit();

        NList<Person> personsWithoutCar = NQL.search(mPerson).search(NQL.all(NQL.has(mPerson.getPersonStatus(), NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS), NQL.has(mPerson.getName(), NQL.Comp.EQUAL, "oasd_*+__asdads3cd& %%"), NQL.hasNull(mPerson.getCar()))).getList();

        Person m2Person = NQL.mock(Person.class);
        System.out.println("NQL.search(m2Person).search(m2Person.getIsSick(), NQL.Comp.EQUAL, false).getList().getNumberFound() = " + NQL.search(m2Person).search(m2Person.getIsSick(), NQL.Comp.EQUAL, false).getList().getNumberFound());
        System.out.println("NQL.search(m2Person).search(m2Person.getIsSick(), NQL.Comp.EQUAL, false).getList().getNumberFound() = " + NQL.search(m2Person).search(m2Person.getIsSick(), NQL.Comp.EQUAL, true).getList().getNumberFound());

//        NQL.Constraint A = NQL.has(m2Person.getHistoryStatus()[NQL.ANY], NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
//        NQL.Constraint Z = NQL.has(mPerson.getPersonStatus(), NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
////        NQL.Constraint A = NQL.has(m2Person.getHistoryStatus()[NQL.ANY], NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
//        NQL.Constraint B = NQL.has(m2Person.getName(), NQL.Comp.EQUAL, "oasd_*+__asdads3cd& %%");
//        NQL.Constraint C = NQL.hasNull(m2Person.getCar());
//        NList<Person> personsWithoutCar2 = NQL.search(m2Person).search(NQL.all(B, C)).getList();
//        //Assert.assertEquals(personsWithoutCar.getNumberFound(), 3);


    }





    @Test
    public void testScore() {
        String[] descs = new String[]{"Næste generation danske Formel 1-håb er allerede i støbeskeen", "12-årige Noah Watt", "gokart-juniormesterskaber i 2015, EM, VM", "person", "Det koster en hulens masse penge at køre det store program"};
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        Person prev = null;
        for (int i = 0; i < 10; i++) {
            Person person = ModelObjectService.create(Person.class);
            if(i % 2 == 0){
                prev = person;
            } else {
                prev.setGirlFriend(person);
                person.setGirlFriend(prev);
            }
//            person.setName("person " + (i % 4) + );
//            person.setDescription(descs[i % descs.length]);
            person.setIsSick(i % 2 == 0);

            person.setPersonStatus(PersonStatus.BETWEEN_RELATIONS);
//            person.setHistoryStatus(new PersonStatus[]{PersonStatus.BETWEEN_RELATIONS, PersonStatus.SINGLE});
            person.setSomeFloat((float) (20f * Math.random()));
            if (i < 7) {
                Car car = ModelObjectService.create(Car.class);
                person.setCar(car);
            }
            ModelObjectService.save(person);
            System.out.println("---------------- PUT START -----------------------");
            ModelObjectSearchService.put(person);
            System.out.println("---------------- PUT END -----------------------");
        }
        solrService.commit();

//        NList<Person> personsWithoutCar = NQL.search(mPerson).search(mPerson.getName(), NQL.Comp.EQUAL, "person").search(mPerson.getDescription(), NQL.Comp.EQUAL, "person").getList();

        Person m2Person = NQL.mock(Person.class);

//        NQL.Constraint A = NQL.has(m2Person.getHistoryStatus()[NQL.ANY], NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
//        NQL.Constraint Z = NQL.has(mPerson.getPersonStatus(), NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
////        NQL.Constraint A = NQL.has(m2Person.getHistoryStatus()[NQL.ANY], NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
//        NQL.Constraint B = NQL.has(m2Person.getName(), NQL.Comp.EQUAL, "oasd_*+__asdads3cd& %%");
//        NQL.Constraint C = NQL.hasNull(m2Person.getCar());
//        NList<Person> personsWithoutCar2 = NQL.search(m2Person).search(NQL.all(B, C)).getList();
//        //Assert.assertEquals(personsWithoutCar.getNumberFound(), 3);


    }




    @Test
    public void testMaxFloat() {
        String[] descs = new String[]{"Næste generation danske Formel 1-håb er allerede i støbeskeen", "12-årige Noah Watt", "gokart-juniormesterskaber i 2015, EM, VM", "person", "Det koster en hulens masse penge at køre det store program"};
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        Person prev = null;
        for (int i = 0; i < 10; i++) {
            Person person = ModelObjectService.create(Person.class);
            if(i % 2 == 0){
                prev = person;
            } else {
                prev.setGirlFriend(person);
                person.setGirlFriend(prev);
            }
            person.setName("person " + (i % 4));
            person.setCountOfCars(i * 100);
//            person.setDescription(descs[i % descs.length]);
            person.setIsSick(i % 2 == 0);

            person.setPersonStatus(PersonStatus.BETWEEN_RELATIONS);
//            person.setHistoryStatus(new PersonStatus[]{PersonStatus.BETWEEN_RELATIONS, PersonStatus.SINGLE});
            person.setSomeFloat((float) (20f * Math.random()));
            if (i < 7) {
                Car car = ModelObjectService.create(Car.class);
                person.setCar(car);
            }
            ModelObjectService.save(person);
            System.out.println("---------------- PUT START -----------------------");
            ModelObjectSearchService.put(person);
            System.out.println("---------------- PUT END -----------------------");
        }
        solrService.commit();

        NStats<Float> stats = NQL.search(mPerson).search(mPerson.getName(), NQL.Comp.EQUAL, "person").getStats(mPerson.getSomeFloat());

        Person m2Person = NQL.mock(Person.class);

//        NQL.Constraint A = NQL.has(m2Person.getHistoryStatus()[NQL.ANY], NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
//        NQL.Constraint Z = NQL.has(mPerson.getPersonStatus(), NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
////        NQL.Constraint A = NQL.has(m2Person.getHistoryStatus()[NQL.ANY], NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
//        NQL.Constraint B = NQL.has(m2Person.getName(), NQL.Comp.EQUAL, "oasd_*+__asdads3cd& %%");
//        NQL.Constraint C = NQL.hasNull(m2Person.getCar());
//        NList<Person> personsWithoutCar2 = NQL.search(m2Person).search(NQL.all(B, C)).getList();
//        //Assert.assertEquals(personsWithoutCar.getNumberFound(), 3);


    }


    @Test
    public void testMaxLong() {
        String[] descs = new String[]{"Næste generation danske Formel 1-håb er allerede i støbeskeen", "12-årige Noah Watt", "gokart-juniormesterskaber i 2015, EM, VM", "person", "Det koster en hulens masse penge at køre det store program"};
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.addTranslateService(new LessismoreTranslateServiceImpl("something", "asda"), Locale.US, new Locale("da"),  new Locale("no"),   new Locale("sv"),   new Locale("de"));
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        Person prev = null;
        for (int i = 0; i < 10; i++) {
            Person person = ModelObjectService.create(Person.class);
            if(i % 2 == 0){
                prev = person;
            } else {
                prev.setGirlFriend(person);
                person.setGirlFriend(prev);
            }
            person.setName("person " + (i % 4));
            person.setCountOfCars(i * 100);
            person.setCountOfFriends((long) (1000 * Math.random()));
//            person.setDescription(descs[i % descs.length]);
            person.setIsSick(i % 2 == 0);

            person.setPersonStatus(PersonStatus.BETWEEN_RELATIONS);
//            person.setHistoryStatus(new PersonStatus[]{PersonStatus.BETWEEN_RELATIONS, PersonStatus.SINGLE});
            person.setSomeFloat((float) (20f * Math.random()));
            if (i < 7) {
                Car car = ModelObjectService.create(Car.class);
                person.setCar(car);
            }
            ModelObjectService.save(person);
            System.out.println("---------------- PUT START -----------------------");
            ModelObjectSearchService.put(person);
            System.out.println("---------------- PUT END -----------------------");
        }
        solrService.commit();

        NStats<Long> stats = NQL.search(mPerson).search(mPerson.getName(), NQL.Comp.EQUAL, "sebastian").getStats(mPerson.getCountOfFriends());


        System.out.println("---------------- RESULTS STARTS -------------");
        System.out.println("stats.getSum() = " + stats.getSum());
        System.out.println("stats.getMax() = " + stats.getMax());
        System.out.println("stats.getMax() = " + stats.getMin());
        System.out.println("stats.getMax() = " + stats.getStddev());
        System.out.println("stats.getMax() = " + stats.getMean());
        Long count = stats.getCount();
        System.out.println("stats.getMax() = " + count);
        System.out.println("---------------- RESULTS ENDS -------------");




//        NQL.Constraint A = NQL.has(m2Person.getHistoryStatus()[NQL.ANY], NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
//        NQL.Constraint Z = NQL.has(mPerson.getPersonStatus(), NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
////        NQL.Constraint A = NQL.has(m2Person.getHistoryStatus()[NQL.ANY], NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
//        NQL.Constraint B = NQL.has(m2Person.getName(), NQL.Comp.EQUAL, "oasd_*+__asdads3cd& %%");
//        NQL.Constraint C = NQL.hasNull(m2Person.getCar());
//        NList<Person> personsWithoutCar2 = NQL.search(m2Person).search(NQL.all(B, C)).getList();
//        //Assert.assertEquals(personsWithoutCar.getNumberFound(), 3);


    }


    @Test
    public void testMaxDouble() {
        String[] descs = new String[]{"Næste generation danske Formel 1-håb er allerede i støbeskeen", "12-årige Noah Watt", "gokart-juniormesterskaber i 2015, EM, VM", "person", "Det koster en hulens masse penge at køre det store program"};
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        Person prev = null;
        for (int i = 0; i < 10; i++) {
            Person person = ModelObjectService.create(Person.class);
            if(i % 2 == 0){
                prev = person;
            } else {
                prev.setGirlFriend(person);
                person.setGirlFriend(prev);
            }
            person.setName("person " + (i % 4));
            person.setCountOfCars(i * 100);
            person.setSomeDouble( (double) (100 * Math.random()) );
            person.setCountOfFriends((long) (1000 * Math.random()));
//            person.setDescription(descs[i % descs.length]);
            person.setIsSick(i % 2 == 0);

            person.setPersonStatus(PersonStatus.BETWEEN_RELATIONS);
//            person.setHistoryStatus(new PersonStatus[]{PersonStatus.BETWEEN_RELATIONS, PersonStatus.SINGLE});
            person.setSomeFloat((float) (20f * Math.random()));
            if (i < 7) {
                Car car = ModelObjectService.create(Car.class);
                person.setCar(car);
            }
            ModelObjectService.save(person);
            System.out.println("---------------- PUT START -----------------------");
            ModelObjectSearchService.put(person);
            System.out.println("---------------- PUT END -----------------------");
        }
        solrService.commit();

        NStats<Double> stats = NQL.search(mPerson).search(mPerson.getName(), NQL.Comp.EQUAL, "person").getStats(mPerson.getSomeDouble());

        Person m2Person = NQL.mock(Person.class);

//        NQL.Constraint A = NQL.has(m2Person.getHistoryStatus()[NQL.ANY], NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
//        NQL.Constraint Z = NQL.has(mPerson.getPersonStatus(), NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
////        NQL.Constraint A = NQL.has(m2Person.getHistoryStatus()[NQL.ANY], NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS);
//        NQL.Constraint B = NQL.has(m2Person.getName(), NQL.Comp.EQUAL, "oasd_*+__asdads3cd& %%");
//        NQL.Constraint C = NQL.hasNull(m2Person.getCar());
//        NList<Person> personsWithoutCar2 = NQL.search(m2Person).search(NQL.all(B, C)).getList();
//        //Assert.assertEquals(personsWithoutCar.getNumberFound(), 3);


    }




    @Test
    public void testCloud() {
        String[] descs = new String[]{"Næste generation danske Person Formel 1-håb er allerede i støbeskeen", "12-årige PersNoah Watt", "gokart-juniormesterskaber personi 2015, EM, VM", "person", "Det koster en hulens masse penge at køre det store program"};
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        Person prev = null;
        for (int i = 0; i < 10; i++) {
            Person person = ModelObjectService.create(Person.class);
            if(i % 2 == 0){
                prev = person;
            } else {
                prev.setGirlFriend(person);
                person.setGirlFriend(prev);
            }
            person.setName("person " + (i % 4));
            person.setCountOfCars(i * 100);
            person.setSomeDouble( (double) (100 * Math.random()) );
            person.setCountOfFriends((long) (1000 * Math.random()));
//            person.setDescription(descs[i % descs.length]);
            person.setIsSick(i % 2 == 0);

            person.setPersonStatus(PersonStatus.BETWEEN_RELATIONS);
//            person.setHistoryStatus(new PersonStatus[]{PersonStatus.BETWEEN_RELATIONS, PersonStatus.SINGLE});
            person.setSomeFloat((float) (20f * Math.random()));
            if (i < 7) {
                Car car = ModelObjectService.create(Car.class);
                person.setCar(car);
            }
            ModelObjectService.save(person);
            System.out.println("---------------- PUT START -----------------------");
            ModelObjectSearchService.put(person);
            System.out.println("---------------- PUT END -----------------------");
        }
        solrService.commit();

        List<Pair<String, Long>> ss = NQL.search(mPerson).search(mPerson.getName(), NQL.Comp.EQUAL, "person").getCloud(mPerson.getName(), 2);
        int k = 0;

    }



    @Test
    public void testValue() {
        Calendar value = Calendar.getInstance();
        System.out.println(value instanceof Calendar);


    }
    @Test
    public void testAnnotation() {
        String[] descs = new String[]{"Næste generation danske Person Formel 1-håb er allerede i støbeskeen", "12-årige PersNoah Watt", "gokart-juniormesterskaber personi 2015, EM, VM", "person", "Det koster en hulens masse penge at køre det store program"};
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        Person prev = null;
        for (int i = 0; i < 10; i++) {
            Person person = ModelObjectService.create(Person.class);
            if(i % 2 == 0){
                prev = person;
            } else {
                prev.setGirlFriend(person);
                person.setGirlFriend(prev);
            }
            person.setName("person " + (i % 4));
            person.setCountOfCars(i * 100);
            person.setSomeDouble((double) (100 * Math.random()));
            person.setCountOfFriends((long) (1000 * Math.random()));
            person.setFun(descs[i % descs.length], new Locale("da"));
            person.setUrl("http://dr.dk/fun");
            System.out.println("FIRST: Should not be null: " + person.getFun());

            person.setIsSick(i % 2 == 0);

            person.setPersonStatus(PersonStatus.BETWEEN_RELATIONS);
//            person.setHistoryStatus(new PersonStatus[]{PersonStatus.BETWEEN_RELATIONS, PersonStatus.SINGLE});
            person.setSomeFloat((float) (20f * Math.random()));
            if (i < 7) {
                Car car = ModelObjectService.create(Car.class);
                person.setCar(car);
            }
            ModelObjectService.save(person);
            System.out.println("---------------- PUT START -----------------------");
            ModelObjectSearchService.put(person);
            System.out.println("---------------- PUT END -----------------------");
        }
        solrService.commit();

        List<Person> list = MQL.select(Person.class).getList();
        for(int i = 0; i < 1; i++){
            System.out.println("Should not be null: " + list.get(i).getFun());
        }

        ObjectCacheFactory.getInstance().getObjectCache(Person.class).clear();
        list = MQL.select(Person.class).getList();
        for(int i = 0; i < 1; i++){
            System.out.println("Should not be null: " + list.get(i).getFun());
        }

        Person mock = NQL.mock(Person.class);
        long count = NQL.search(mock).search(mock.getUrl(), NQL.Comp.EQUAL, "http://dr.dk/").getCount();
        System.out.println("count = " + count);


    }







}
