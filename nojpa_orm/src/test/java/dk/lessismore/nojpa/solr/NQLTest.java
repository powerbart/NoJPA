package dk.lessismore.nojpa.solr;

import dk.lessismore.nojpa.db.methodquery.NList;
import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.db.methodquery.NStats;
import dk.lessismore.nojpa.db.testmodel.Car;
import dk.lessismore.nojpa.db.testmodel.DatabaseCreatorTest;
import dk.lessismore.nojpa.db.testmodel.Person;
import dk.lessismore.nojpa.db.testmodel.PersonStatus;
import dk.lessismore.nojpa.reflection.db.DatabaseCreator;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectSearchService;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import dk.lessismore.nojpa.reflection.db.model.SolrServiceImpl;
import dk.lessismore.nojpa.utils.Pair;
import junit.framework.Assert;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.Test;

import java.util.*;

/**
 * Created by niakoi on 7/23/14.
 */
public class NQLTest {

    public static void main(String[] args) {
        System.out.println("asdasda:ąčęėįšųūžæøå".replaceAll("[^\\u0000-\\u02B8\\u0390-\\u057F]", ""));
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
            person.setDescription("Hello:ąčęėįšųūž");

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

        for(int i = 0; i < persons.size(); i++){
            System.out.println("********************** " + persons.get(i).getDescription());
        }




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
                    System.out.println("facetField.getGap = " + facetField.getGap());
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
                    System.out.println("facetField.getEnd() = " + facetField.getEnd());
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
                    System.out.println("facetField.getGap = " + facetField.getGap());
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
                    System.out.println("facetField.getEnd() = " + facetField.getEnd());
                    System.out.println("--------------------------- END");
                }
            }


        }

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
            person.setDescription(descs[i % descs.length]);
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

        NList<Person> personsWithoutCar = NQL.search(mPerson).search(mPerson.getName(), NQL.Comp.EQUAL, "person").search(mPerson.getDescription(), NQL.Comp.EQUAL, "person").getList();

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
            person.setDescription(descs[i % descs.length]);
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
            person.setDescription(descs[i % descs.length]);
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

        NStats<Long> stats = NQL.search(mPerson).search(mPerson.getName(), NQL.Comp.EQUAL, "person").getStats(mPerson.getCountOfFriends());

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
            person.setDescription(descs[i % descs.length]);
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
            person.setDescription(descs[i % descs.length]);
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

        List<Pair<String, Long>> ss = NQL.search(mPerson).search(mPerson.getName(), NQL.Comp.EQUAL, "person").getFacet(mPerson.getName(), 2);

//        ArrayList<FacetField.Count> tags = new ArrayList<FacetField.Count>();
//        try {
//            SolrQuery query = new SolrQuery();
//            //query.setQuery("(_ArticleRunner_article__ID_Article_tags__TXT_ARRAY_Tag_name__TXT_ARRAY:("+ q+" ))");
//            query.setQuery("*:*");
////            if(!filterQ.isEmpty()) {
////                query.addFilterQuery(filterQ); //only active
////            }
//            query.setFacet(true);
//            query.setFacetLimit(40);
//            query.addFacetField("_Person_isSick__BOOL");
////            query.addFacetPivotField("_Person_isSick__BOOL, _Person_countOfFriends__LONG");
////            query.addFacetPivotField();
//
////            query.addFacetField("");
//            QueryResponse response = solrService.query(query);
//            List<FacetField.Count> facets = response.getFacetFields().get(0).getValues();
//            for (FacetField.Count facet : facets) {
//                if(facet.getCount() > 0) {
//                    tags.add(facet);
//                }
//            }
//            int k = 0;
//
//        } catch (Exception e) {
//            System.out.println("cloud: got some error, ignore it: " + e.getMessage());
//            e.printStackTrace();
//        }

        int k = 0;



    }





}
