package dk.lessismore.nojpa.solr;

import dk.lessismore.nojpa.cache.ObjectCacheFactory;
import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.db.methodquery.NList;
import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.db.methodquery.NStats;
import dk.lessismore.nojpa.db.testmodel.*;
import dk.lessismore.nojpa.reflection.db.DatabaseCreator;
import dk.lessismore.nojpa.reflection.db.DbObjectVisitor;
import dk.lessismore.nojpa.reflection.db.model.CloudSolrServiceImpl;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectSearchService;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import dk.lessismore.nojpa.reflection.db.model.SolrService;
import dk.lessismore.nojpa.reflection.db.model.SolrServiceImpl;
import dk.lessismore.nojpa.reflection.translate.LessismoreTranslateServiceImpl;
import dk.lessismore.nojpa.utils.Pair;
import junit.framework.Assert;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.IntervalFacet;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

/**
 * Created by seb on 7/23/14.
 */
public class NQLTest {

    public static void main(String[] args) {
        System.out.println("asdasda:ąčęėįšųūžæøå".replaceAll("[^\\u0000-\\u02B8\\u0390-\\u057F]", ""));
    }



    @Test
    public void testBig10() {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        for (int i = 0; i < 10; i++) {
            Person person = ModelObjectService.create(Person.class);
            person.setName("person " + (i % 4));
            ModelObjectService.save(person);
            ModelObjectSearchService.put(person);

        }
    }


    @Test
    public void testDbCreateInlineWithVisitor() throws Exception {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");

        Class<Address> addressClass = Address.class;
        List<Class> l = new ArrayList<Class>();
        l.add(addressClass);
        DatabaseCreator.createDatabase(l);

        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Address.class, solrService.getServer());


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
//        {
//            Address addressN = ModelObjectService.create(Address.class);
//            addressN.setArea("NothingArea");
//            ModelObjectService.save(addressN);
//
//        }
        ModelObjectService.save(address);

        {
            ObjectCacheFactory.getInstance().getObjectCache(Address.class).clear();
            ObjectCacheFactory.getInstance().getObjectCache(Phone.class).clear();
            System.out.println("------------- START");
            List<Address> list = MQL.select(Address.class).getList();
            for (Address a : list) {
                System.out.printf("a->" + (a.getA() != null ? a.getA().getNumber() : "null"));

            }
            System.out.println("------------- END");

            {
                ObjectCacheFactory.getInstance().getObjectCache(Address.class).clear();
                ObjectCacheFactory.getInstance().getObjectCache(Phone.class).clear();
                Address mock = MQL.mock(Address.class);
                List<Address> myArea = MQL.select(mock).where(mock.getArea(), MQL.Comp.EQUAL, "MyArea").getList();
                System.out.println(myArea.size());
            }

            {
                ObjectCacheFactory.getInstance().getObjectCache(Address.class).clear();
                ObjectCacheFactory.getInstance().getObjectCache(Phone.class).clear();
                Address mock = MQL.mock(Address.class);
                List<Address> myArea = MQL.select(mock).where(mock.getA().getNumber(), MQL.Comp.EQUAL, "MyNumberRocks!").getList();
                System.out.println(myArea.size());
            }
            {

                Address mAddress = MQL.mock(Address.class);
                MQL.select(mAddress).where(mAddress.getCreationDate(), MQL.Comp.EQUAL_OR_GREATER, Calendar.getInstance()).visit(new DbObjectVisitor() {
                    @Override
                    public void visit(ModelObjectInterface m) {
                        System.out.println("visiting ... ");
                    }

                    @Override
                    public void setDone(boolean b) {

                    }

                    @Override
                    public boolean getDone() {
                        return false;
                    }
                });


            }

        }
    }



    @Test
    public void testDbCreateInlineWithSolr() throws Exception {
        Class<Address> addressClass = Address.class;
        List<Class> l = new ArrayList<Class>();
        l.add(addressClass);
        DatabaseCreator.createDatabase(l);

        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Address.class, solrService.getServer());


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
//        {
//            Address addressN = ModelObjectService.create(Address.class);
//            addressN.setArea("NothingArea");
//            ModelObjectService.save(addressN);
//
//        }
        ModelObjectService.save(address);

        {
            ObjectCacheFactory.getInstance().getObjectCache(Address.class).clear();
            ObjectCacheFactory.getInstance().getObjectCache(Phone.class).clear();
            System.out.println("------------- START");
            List<Address> list = MQL.select(Address.class).getList();
            for (Address a : list) {
                System.out.printf("a->" + (a.getA() != null ? a.getA().getNumber() : "null"));

            }
            System.out.println("------------- END");

            {
                ObjectCacheFactory.getInstance().getObjectCache(Address.class).clear();
                ObjectCacheFactory.getInstance().getObjectCache(Phone.class).clear();
                Address mock = MQL.mock(Address.class);
                List<Address> myArea = MQL.select(mock).where(mock.getArea(), MQL.Comp.EQUAL, "MyArea").getList();
                System.out.println(myArea.size());
            }

            {
                ObjectCacheFactory.getInstance().getObjectCache(Address.class).clear();
                ObjectCacheFactory.getInstance().getObjectCache(Phone.class).clear();
                Address mock = MQL.mock(Address.class);
                List<Address> myArea = MQL.select(mock).where(mock.getA().getNumber(), MQL.Comp.EQUAL, "MyNumberRocks!").getList();
                System.out.println(myArea.size());
            }
        }
        {
            System.out.println("------------- Adding to Solr START");
            List<Address> list = MQL.select(Address.class).getList();
            for (Address a : list) {
                System.out.printf("a->" + (a.getA() != null ? a.getA().getNumber() : "null"));
                ModelObjectSearchService.put(a);

            }
            System.out.println("------------- Adding to Solr END");
        }
        ModelObjectSearchService.commit(Address.class);
        {
            System.out.println("------------- Getting from Solr START");
            Address mock = NQL.mock(Address.class);
            NList<Address> list = NQL.search(mock).getList();
            for (Address a : list) {
                System.out.printf("a->" + (a.getA() != null ? a.getA().getNumber() : "null"));

            }
            System.out.println("------------- Getting from Solr END");
        }
        {
            System.out.println("------------- Getting specific START");
            Address mock = NQL.mock(Address.class);
            NList<Address> list = NQL.search(mock).search(mock.getA().getFunnyD(), NQL.Comp.EQUAL_OR_GREATER, 200d).getList();
            for (Address a : list) {
                System.out.printf("a->" + (a.getA() != null ? a.getA().getNumber() : "null"));

            }
            System.out.println("------------- Getting specific Solr END");
        }


    } //_Address_a__ID_Phone_funnyD__DOUBLE
      //_Address_a__ID_Phone_funnyD__DOUBLE


//
    @Test
    public void testCloudSolr() throws Exception {
        SolrService solrService = new CloudSolrServiceImpl("86.58.206.113:2181", "product");
        solrService.getServer();
        QueryResponse response = solrService.query(new SolrQuery("*:*"));
        System.out.println("response = " + response);
    }


    @Test
    public void testBoolean2() throws InterruptedException {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person person = ModelObjectService.create(Person.class);
        person.setName("person name");

        Car c1 = ModelObjectService.create(Car.class);
        Car c2 = ModelObjectService.create(Car.class);
        person.setCar(c1);

        Thread.sleep(30);
        System.out.println("saving - 1");
        ModelObjectService.save(person);
        person.setIsSick(false);
        Thread.sleep(30);
        System.out.println("saving - 2");
        ModelObjectService.save(person);
        person.setIsSick(true);
//        person.setName("sdfsdfsdf");
        Thread.sleep(30);
        System.out.println("saving - 3");
        ModelObjectService.save(person);
        System.out.println("saving - DONE - boolean");
        System.out.println("saving - START - association");
        person.setCar(c2);
        ModelObjectService.save(person);
        System.out.println("saving - DONE");

    }



        @Test
    public void test01() {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        for (int i = 0; i < 10; i++) {
            Person person = ModelObjectService.create(Person.class);
            person.setName("person " + (i % 4));
//            person.setDescription("Hello:ąčęėįšųūž");

            person.setSomeFloat((float) (20f * Math.random()));
            if (i < 7) {
                Car car = ModelObjectService.create(Car.class);
                person.setCar(car);
            }
            ModelObjectService.save(person);
            ModelObjectSearchService.put(person);
        }
        solrService.commit();

        NList<Person> personsWithoutCar = NQL.search(mPerson).search(NQL.all(NQL.has(mPerson.getName(), NQL.Comp.EQUAL, "Person"), NQL.hasNull(mPerson.getCar()))).getList();
        Assert.assertEquals(personsWithoutCar.getNumberFound(), 3);

        NList<Person> personsWithoutCar2 = NQL.search(mPerson).searchIsNull(mPerson.getCar()).getList();
        Assert.assertEquals(personsWithoutCar2.getNumberFound(), 3);

        NList<Person> personsWithCar = NQL.search(mPerson).search(NQL.hasNotNull(mPerson.getCar())).getList();
        Assert.assertEquals(personsWithCar.getNumberFound(), 7);

        NList<Person> persons = NQL.search(mPerson).getList();
        Assert.assertEquals(persons.getNumberFound(), 10);

//        for(int i = 0; i < persons.size(); i++){
//            System.out.println("********************** " + persons.get(i).getDescription());
//        }




        NList<Person> personsWithcarWithoutAddress = NQL.search(mPerson).searchIsNull(mPerson.getCar().getAddress()).getList();
//        Assert.assertEquals(personsWithcarWithoutAddress.getNumberFound(), 3);

        {
            QueryResponse response = solrService.query(new SolrQuery("( (_Person_name__TXT:( Person )) AND -(_Person_car__ID:[\"\" TO *]) )"));
            Assert.assertEquals(response.getResults().getNumFound(), 3);
        }

        {

            String[] ss = new String[]{"Brian", "Das", "Mas", "Agne", "Carl", "Stefan", "Atanas", "Michael", "Camilla", "Sebastian"};
            for (int i = 0; i < 100; i++) {
                Person person = ModelObjectService.create(Person.class);
                person.setName(ss[(int) (10f * Math.random())]);
                person.setSomeFloat((float) (20f * Math.random()));
                if (i < 7) {
                    Car car = ModelObjectService.create(Car.class);
                    person.setCar(car);
                }
                ModelObjectService.save(person);
                ModelObjectSearchService.put(person);
            }
            solrService.commit();

            {
                SolrQuery query = new SolrQuery();
                query.setQuery("*:*");
                query.setFacet(true);
                query.addFacetQuery("_Person_someFloat__DOUBLE:[* TO 5]");
                query.addFacetQuery("_Person_someFloat__DOUBLE:[5 TO 15]");
                query.addFacetQuery("_Person_someFloat__DOUBLE:[15 TO *]");
                //query.addFacetField("_Person_name__TXT");
//                query.addFacetField("_Person_someFloat__DOUBLE");
                query.setFacetLimit(12);
//            query.set(FacetParams.FACET_QUERY, "_Person_name__TXT:person");
//            BoboRequestBuilder.applyFacetExpand(query, "color", true);

                QueryResponse response = solrService.query(query);

                System.out.println("response.getStatus() = " + response.getStatus());
                System.out.println("response.getResults().getNumFound() = " + response.getResults().getNumFound());
                System.out.println("response.getFacetQuery().size() = " + response.getFacetQuery().size());
                System.out.println("response.getFacetQuery().size() = " + response.getFacetFields().size());



                for(Iterator<String> iterator = response.getFacetQuery().keySet().iterator(); iterator.hasNext();  ){
                    String next = iterator.next();
                    System.out.println(next + " -> " + response.getFacetQuery().get(next));
                }


                for (int k = 0; k < response.getFacetFields().size(); k++) {
                    FacetField facetField = response.getFacetFields().get(k);
                    System.out.println("--------------------------- START");
                    System.out.println("facetField.getName() = " + facetField.getName());
                    System.out.println("facetField.getValueCount() = " + facetField.getValueCount());
                    List<FacetField.Count> facetFieldValues = facetField.getValues();
                    for (int h = 0; h < facetFieldValues.size(); h++) {
                        FacetField.Count c = facetFieldValues.get(h);
                        System.out.println("c.getName() = " + c.getName());
                        System.out.println("c.getAsFilterQuery() = " + c.getAsFilterQuery());
                        System.out.println("c.getCount() = " + c.getCount());
                        System.out.println("c.getFacetField() = " + c.getFacetField());
                    }
                    System.out.println("--------------------------- END");
                }
                for (int k = 0; k < response.getIntervalFacets().size(); k++) {
                    IntervalFacet facetField = response.getIntervalFacets().get(k);
                    System.out.println("--------------------------- START");
                    System.out.println("facetField.getField() = " + facetField.getField());
                    System.out.println("facetField.getIntervals() = " + facetField.getIntervals());
                    System.out.println("--------------------------- END");
                }
                for (int k = 0; k < response.getFacetRanges().size(); k++) {
                    RangeFacet facetField = response.getFacetRanges().get(k);
                    System.out.println("--------------------------- START");
                    System.out.println("facetField.getName() = " + facetField.getName());
                    System.out.println("facetField.getBefore() = " + facetField.getBefore());
                    System.out.println("facetField.getAfter() = " + facetField.getAfter());
                    System.out.println("facetField.getGap() = " + facetField.getGap());
                    System.out.println("facetField.getStart() = " + facetField.getStart());
                    System.out.println("facetField.getEnd() = " + facetField.getEnd());
                    System.out.println("facetField.getBetween() = " + facetField.getBetween());
                    System.out.println("facetField.getCounts() = " + facetField.getCounts());
                    System.out.println("--------------------------- END");
                }
            }

            {
                SolrQuery query = new SolrQuery();
                query.setQuery("*:*");
                query.setFacet(true);
                query.addFacetField("_Person_name__TXT");
                query.setFacetLimit(12);
//            query.set(FacetParams.FACET_QUERY, "_Person_name__TXT:person");
//            BoboRequestBuilder.applyFacetExpand(query, "color", true);

                QueryResponse response = solrService.query(query);

                System.out.println("response.getStatus() = " + response.getStatus());
                System.out.println("response.getResults().getNumFound() = " + response.getResults().getNumFound());
                System.out.println("response.getFacetQuery().size() = " + response.getFacetQuery().size());
                System.out.println("response.getFacetQuery().size() = " + response.getFacetFields().size());
                for (int k = 0; k < response.getFacetFields().size(); k++) {
                    FacetField facetField = response.getFacetFields().get(k);
                    System.out.println("--------------------------- START");
                    System.out.println("facetField.getName() = " + facetField.getName());
                    System.out.println("facetField.getValueCount() = " + facetField.getValueCount());
                    List<FacetField.Count> facetFieldValues = facetField.getValues();
                    for (int h = 0; h < facetFieldValues.size(); h++) {
                        FacetField.Count c = facetFieldValues.get(h);
                        System.out.println("c.getName() = " + c.getName());
                        System.out.println("c.getAsFilterQuery() = " + c.getAsFilterQuery());
                        System.out.println("c.getCount() = " + c.getCount());
                        System.out.println("c.getFacetField() = " + c.getFacetField());
                    }
                    System.out.println("--------------------------- END");
                }
            }


        }

    }






    @Test
    public void testObjectID_simpleRelation() {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        Person prev = null;
        for (int i = 0; i < 2; i++) {
            Person person = ModelObjectService.create(Person.class);
            if(i % 2 == 0){
                prev = person;
            } else {
                prev.setGirlFriend(person);
                person.setGirlFriend(prev);
            }
            Address address = ModelObjectService.create(Address.class);
            ModelObjectService.save(address);
            person.setAddresses(new Address[]{address});
            person.setName("person " + (i % 4));
            person.setIsSick(i % 2 == 0);

            if (i % 2 == 0) {
                Calendar birthDate = Calendar.getInstance();
                birthDate.add(Calendar.YEAR, i * -1);
                person.setBirthDate(birthDate);
            }
            person.setPersonStatus(PersonStatus.BETWEEN_RELATIONS);
//            person.setHistoryStatus(new PersonStatus[]{PersonStatus.BETWEEN_RELATIONS, PersonStatus.SINGLE});
            person.setSomeFloat((float) (20f * Math.random()));
            if (i < 7) {
                Car car = ModelObjectService.create(Car.class);
                person.setCar(car);
            }
            ModelObjectService.save(person);
            ModelObjectSearchService.put(person);
            ModelObjectSearchService.commit(person);
        }
        
        {
            Person mock = NQL.mock(Person.class);
            int size = NQL.search(mock).search(mock.getGirlFriend(), NQL.Comp.EQUAL, prev).getList().size();
            Assert.assertEquals(size, 1);

        }

        //_Person_girlFriend__ID= 13CE568292B5FF208BDEB8F439D5E71C)
        //_Person_girlFriend__ID:(13CE568292B5FF208BDEB8F439D5E71C)


    }

    @Test
    public void testGetAttributeOnMock() {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        Person prev = null;
        for (int i = 0; i < 2; i++) {
            Person person = ModelObjectService.create(Person.class);
            if(i % 2 == 0){
                prev = person;
            } else {
                prev.setGirlFriend(person);
                person.setGirlFriend(prev);
            }
            Address address = ModelObjectService.create(Address.class);
            ModelObjectService.save(address);
            person.setAddresses(new Address[]{address});
            person.setName("person " + (i % 4));
            person.setIsSick(i % 2 == 0);

            if (i % 2 == 0) {
                Calendar birthDate = Calendar.getInstance();
                birthDate.add(Calendar.YEAR, i * -1);
                person.setBirthDate(birthDate);
            }
            person.setPersonStatus(PersonStatus.BETWEEN_RELATIONS);
//            person.setHistoryStatus(new PersonStatus[]{PersonStatus.BETWEEN_RELATIONS, PersonStatus.SINGLE});
            person.setSomeFloat((float) (20f * Math.random()));
            if (i < 7) {
                Car car = ModelObjectService.create(Car.class);
                person.setCar(car);
            }
            ModelObjectService.save(person);
            ModelObjectSearchService.put(person);
            ModelObjectSearchService.commit(person);
        }

        {
            Person mock = NQL.mock(Person.class);
            int size = NQL.search(mock).search((Person) ModelObjectService.getAttributeValue(mock, "girlFriend"), NQL.Comp.EQUAL, prev).getList().size();
            Assert.assertEquals(size, 1);

        }

    }

    @Test
    public void testObjectID_complexRelation() throws InterruptedException {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        Person prev = null;
        Address firstAddress = null;
        for (int i = 0; i < 2; i++) {
            Person person = ModelObjectService.create(Person.class);
            if(i % 2 == 0){
                prev = person;
            } else {
                prev.setGirlFriend(person);
                person.setGirlFriend(prev);
            }
            Address address = ModelObjectService.create(Address.class);
            if(firstAddress == null) {
                firstAddress = address;
            }
            ModelObjectService.save(address);
            person.setAddresses(new Address[]{address});
            person.setName("person " + (i % 4));
            person.setIsSick(i % 2 == 0);

            if (i % 2 == 0) {
                Calendar birthDate = Calendar.getInstance();
                birthDate.add(Calendar.YEAR, i * -1);
                person.setBirthDate(birthDate);
            }
            person.setPersonStatus(PersonStatus.BETWEEN_RELATIONS);
//            person.setHistoryStatus(new PersonStatus[]{PersonStatus.BETWEEN_RELATIONS, PersonStatus.SINGLE});
            person.setSomeFloat((float) (20f * Math.random()));
            if (i < 7) {
                Car car = ModelObjectService.create(Car.class);
                person.setCar(car);
            }
            ModelObjectService.save(person);
            ModelObjectSearchService.put(person);
            ModelObjectSearchService.commit(person);
            Thread.sleep(20);
        }

        {
            Person mock = NQL.mock(Person.class);
            int size = NQL.search(mock).search(mock.getAddresses()[MQL.ANY], NQL.Comp.EQUAL, firstAddress).getList().size();
            Assert.assertEquals(size, 1);
        }
        //_Person_addresses__TXT_ARRAY_Address__ID_ARRAY:(13CE596B32874708FDC69E5006107BC4)
        //_Person_addresses__TXT_ARRAY_Address__ID_ARRAY:(13CE596B386F31D051339156467D3284)

    }

    @Test
    public void testObjectID_noRelation() {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        Person prev = null;
        for (int i = 0; i < 2; i++) {
            Person person = ModelObjectService.create(Person.class);
            if(i % 2 == 0){
                prev = person;
            } else {
                prev.setGirlFriend(person);
                person.setGirlFriend(prev);
            }
            Address address = ModelObjectService.create(Address.class);
            ModelObjectService.save(address);
            person.setAddresses(new Address[]{address});
            person.setName("person " + (i % 4));
            person.setIsSick(i % 2 == 0);

            if (i % 2 == 0) {
                Calendar birthDate = Calendar.getInstance();
                birthDate.add(Calendar.YEAR, i * -1);
                person.setBirthDate(birthDate);
            }
            person.setPersonStatus(PersonStatus.BETWEEN_RELATIONS);
//            person.setHistoryStatus(new PersonStatus[]{PersonStatus.BETWEEN_RELATIONS, PersonStatus.SINGLE});
            person.setSomeFloat((float) (20f * Math.random()));
            if (i < 7) {
                Car car = ModelObjectService.create(Car.class);
                person.setCar(car);
            }
            ModelObjectService.save(person);
            ModelObjectSearchService.put(person);
            ModelObjectSearchService.commit(person);
        }

        {
            Person mock = NQL.mock(Person.class);
            long numberFound = NQL.search(mock).search(mock.getObjectID(), NQL.Comp.EQUAL, prev.getObjectID()).getList().getNumberFound();
            Assert.assertEquals(numberFound, 1);
        }

        //_Person_girlFriend__ID= 13CE568292B5FF208BDEB8F439D5E71C)
        //_Person_girlFriend__ID:(13CE568292B5FF208BDEB8F439D5E71C)


    }



    @Test
    public void testObjectID() throws InterruptedException {
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


//        {
//            Person m2Person = NQL.mock(Person.class);
//            long some = NQL.search(m2Person).search(mPerson.getName(), NQL.Comp.EQUAL, "Some").getCount();
//        }
//        Thread.sleep(1);
//        System.out.println("-------------------------------------- ***** 1");
//        {
//            Person m2Person = NQL.mock(Person.class);
//            long some = NQL.search(m2Person).search(mPerson.getGirlFriend().getObjectID(), NQL.Comp.EQUAL, prev.getObjectID()).getCount();
//        }
//        Thread.sleep(1);
//        System.out.println("-------------------------------------- ***** 2");
//        {
//            Person m2Person = NQL.mock(Person.class);
//            long some = NQL.search(m2Person).search(mPerson.getGirlFriend(), NQL.Comp.EQUAL, prev).getCount();
//        }
//        Thread.sleep(1);
//        System.out.println("-------------------------------------- ***** 3");  ATANAS IS GREAT !!!!!

        {
            Person m2Person = NQL.mock(Person.class);
            long numberFound = NQL.search(m2Person).search(mPerson.getObjectID(), NQL.Comp.NOT_EQUAL, prev.getObjectID()).getCount();
            Assert.assertEquals(numberFound, 9);
            System.out.println("numberFound = " + numberFound);
        }
//        {
//            String q = "-(objectID:(" + prev.getObjectID() + ") )";
//            QueryResponse response = solrService.query(new SolrQuery(q));
//            System.out.println("response.getResults().getNumFound(): ALL - 1 ["+ q +"]= " + response.getResults().getNumFound());
//        }

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

            if (i % 2 == 0) {
                Calendar birthDate = Calendar.getInstance();
                birthDate.add(Calendar.YEAR, i * -1);
                person.setBirthDate(birthDate);
            }
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


        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.YEAR, -5);
        NList<Person> list = NQL.search(m2Person).search(
                NQL.not(
                        NQL.all(
                                NQL.not(NQL.has(m2Person.getBirthDate(), NQL.Comp.EQUAL_OR_LESS, instance)),
                                NQL.hasNotNull(m2Person.getBirthDate())))
        ).getList();
        for (Person person : list) {
            System.out.println("person.getBirthDate().getTime() = " + person.getBirthDate());
        }
        System.out.println("count = " + list.size());


    }






    @Test
    public void testReadRawFromSolr() {
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

            if (i % 2 == 0) {
                Calendar birthDate = Calendar.getInstance();
                birthDate.add(Calendar.YEAR, i * -1);
                person.setBirthDate(birthDate);
            }
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

        NList<Person> personsWithoutCar = NQL.search(mPerson).search(NQL.all(NQL.has(mPerson.getPersonStatus(), NQL.Comp.EQUAL, PersonStatus.BETWEEN_RELATIONS), NQL.has(mPerson.getName(), NQL.Comp.EQUAL, "per*3"), NQL.hasNull(mPerson.getCar()))).getList();
        System.out.println("count = " + personsWithoutCar.getNumberFound());
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


        Person m2Person = NQL.mock(Person.class);



    }



    @Test
    public void testFunnyChars() throws InterruptedException {
        System.out.println(NQL.removeFunnyChars("wegner and sort"));
    }

    @Test
    public void testFunnyChars2() throws InterruptedException {
        System.out.println(NQL.removeFunnyChars("wegner and and or"));
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
    public void testBoolean() {
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
            person.setDescription(i % 4 == 0 ? "adadasd" : null);
            person.setIsSick(i % 2 == 0);
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


        NList<Person> list = NQL.search(mPerson).search(NQL.all(NQL.has(mPerson.getIsSick(), NQL.Comp.EQUAL, true), NQL.has(mPerson.getName(), NQL.Comp.EQUAL, "person"))).searchIsNull(mPerson.getDescription()).getList();
        System.out.println("list.getNumberFound() = " + list.getNumberFound());

    }



    @Test
    public void testValue() {
        Calendar value = Calendar.getInstance();
        System.out.println(value instanceof Calendar);


    }



    @Test
    public void testAnnotation() throws SolrServerException, IOException {
        String[] descs = new String[]{"Næste generationer danske Person Formel 1-håb er allerede i støbeskeen", "12-årige PersNoah Watt allerede", "gokart-juniormesterskaber personi 2015, EM, VM", "person allerede", "Det koster en hulens masse penge at køre det store program"};
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        Person prev = null;
        for (int i = 0; i < 50; i++) {
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
            person.setFun(descs[i % descs.length] + (i % 3 == 0 ? " abe" : " tiger"), new Locale("da"));
            person.setUrl("http://dr.dk/fun");
            System.out.println("FIRST: Should not be null: " + person.getFun());
            person.setAddresses(new Address[]{ModelObjectService.create(Address.class)});
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
        ObjectCacheFactory.getInstance().getObjectCache(Person.class).clear();

//        List<Person> list = MQL.select(Person.class).getList();
//        for(int i = 0; i < 1; i++){
//            System.out.println("Should not be null: " + list.get(i).getFun());
//        }
//
//        ObjectCacheFactory.getInstance().getObjectCache(Person.class).clear();
//        list = MQL.select(Person.class).getList();
//        for(int i = 0; i < 1; i++){
//            System.out.println("Should not be null: " + list.get(i).getFun());
//        }
//        {
//            Person mock = NQL.mock(Person.class);
//            long count = NQL.search(mock).search(mock.getUrl(), NQL.Comp.EQUAL, "http://dr.dk/").getCount();
//            System.out.println("count = " + count);
//        }
        {
            Person mock = NQL.mock(Person.class);
            NList<Person> nList = NQL.search(mock).search(mock.getCountOfCars(), NQL.Comp.EQUAL_OR_GREATER, 0).search(NQL.all(NQL.has(mock.getFun(), NQL.Comp.EQUAL, "generationer"), NQL.has(mock.getFun(), NQL.Comp.EQUAL, "allerede"), NQL.has(mock.getFun(), NQL.Comp.NOT_EQUAL, "tiger"))).getList();
            long count = nList.getNumberFound();
            System.out.println("count = " + count);
            for(int i = 0; i < 100 && i < nList.size(); i++){
                System.out.println(i + "::" + nList.get(i).getFun());
            }
        }
        System.out.println(" ============================= ");
        {
            SolrClient solrServer = ModelObjectSearchService.solrServer(Person.class);
            SolrQuery solrQuery = new SolrQuery("(_Person_countOfCars__INT:[0 TO *]) ( (_Person_fun___da_TXT:( generationer AND allerede AND tiger )) )");
            solrQuery.setFields("*, score, _explain_");
            QueryResponse queryResponse = solrServer.query(solrQuery);
            int size = queryResponse.getResults().size();
            System.out.println("RAW--size = " + size);
            for(int i = 0; i < size; i++){
                SolrDocument entries = queryResponse.getResults().get(i);
//                    if(i == 0){
//                        Iterator<String> iterator = entries.getFieldNames().iterator();
//                        for(; iterator.hasNext() ;){
//                            String next = iterator.next();
//                            log.debug("Fieldnames:" + next);
//                        }
//                    }
                String objectID = entries.get("objectID").toString();   //
                System.out.println(i + "::" + entries.get("_Person_fun___da_TXT").toString());
            }
        }

    }



    public static int countOfVisit = 0;


    @Test
    public void testVisitor() throws SolrServerException {
        String[] descs = new String[]{"Næste generationer danske Person Formel 1-håb er allerede i støbeskeen", "12-årige PersNoah Watt allerede", "gokart-juniormesterskaber personi 2015, EM, VM", "person allerede", "Det koster en hulens masse penge at køre det store program"};
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());
        Person mPerson = NQL.mock(Person.class);

        Person prev = null;
        for (int i = 0; i < 50; i++) {
            Person person = ModelObjectService.create(Person.class);
            person.setName("person " + (i % 4));
            Address address = ModelObjectService.create(Address.class);
            address.setZip(2860);
            person.setAddresses(new Address[]{address});

            person.setCountOfCars(i * 100);
            person.setSomeDouble((double) (100 * Math.random()));
            person.setCountOfFriends((long) (1000 * Math.random()));
            person.setFun(descs[i % descs.length] + (i % 3 == 0 ? " abe" : " tiger"), new Locale("da"));
            person.setUrl("http://dr.dk/fun");
            person.setIsSick(i % 2 == 0);
            person.setPersonStatus(PersonStatus.BETWEEN_RELATIONS);
            person.setSomeFloat((float) (20f * Math.random()));
            ModelObjectService.save(person);
        }

        final DbObjectVisitor visitor = new DbObjectVisitor() {


            @Override
            public void visit(ModelObjectInterface m) {
                countOfVisit++;
                try {
                    System.out.println("VISIT(m) = " + m);
                    Thread.sleep(45 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.out.println("RUNNING("+ countOfVisit +") visit of: " + m);
            }

            @Override
            public void setDone(boolean b) {

            }

            @Override
            public boolean getDone() {
                return false;
            }
        };


        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                Person mock = MQL.mock(Person.class);
                MQL.select(mock).where(mock.getAddresses()[MQL.ANY].getZip(), MQL.Comp.EQUAL, 2860).visit(visitor, 2);
            }
        });
        t1.start();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < 280; i++){
                    System.out.println("running(i) = " + i);
                    Person mock = MQL.mock(Person.class);
                    MQL.select(mock).getFirst();
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t2.start();


        while(t1.isAlive() && t2.isAlive()){
            try {
                Thread.sleep(45 * 1000);
                System.out.println("w...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Called visitor.count("+ countOfVisit +")");

    }

    @Test
    public void testMinMaxScores() {
        // <------Prepearation------>
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
            String personNumber = "";
            for (int j = 0; j < 10; j++) {
                personNumber += (int) (2 * Math.random()) + " ";
            }
            person.setName("person " + personNumber);
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
        // <------ END Prepearation------>

        // <------TEST------>
        NQL.search(mPerson).search(mPerson.getName(), NQL.Comp.LIKE, "1").getList(); //
        NList<Person> scoreMax = NQL.search(mPerson).search(mPerson.getName(), NQL.Comp.LIKE, "1").scoreMax(1.5f).getList();
        NList<Person> scoreMin = NQL.search(mPerson).search(mPerson.getName(), NQL.Comp.LIKE, "1").scoreMin(1.1f).getList();
        NList<Person> scoreWithin = NQL.search(mPerson).search(mPerson.getName(), NQL.Comp.LIKE, "1").scoreWithin(0.5f, 1.5f).getList();
        // <------END TEST------>
    }






}
