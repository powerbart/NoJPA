package dk.lessismore.nojpa.solr;

import dk.lessismore.nojpa.db.methodquery.NList;
import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.db.testmodel.Car;
import dk.lessismore.nojpa.db.testmodel.DatabaseCreatorTest;
import dk.lessismore.nojpa.db.testmodel.Person;
import dk.lessismore.nojpa.reflection.db.DatabaseCreator;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectSearchService;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import dk.lessismore.nojpa.reflection.db.model.SolrServiceImpl;
import org.junit.Test;

/**
 * Created by niakoi on 7/23/14.
 */
public class NQLTest {
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
            person.setName("person: " + i);
            if (i < 7) {
                Car car = ModelObjectService.create(Car.class);
                person.setCar(car);
            }
            ModelObjectService.save(person);
            ModelObjectSearchService.put(person);
        }
        solrService.commit();

        NList<Person> personsWithoutCar = NQL.search(mPerson).searchIsNull(mPerson.getCar()).getList();
        System.out.println("personsWithoutCar.getNumberFound() = " + personsWithoutCar.getNumberFound());
        for (Person person : personsWithoutCar) {
            System.out.println("WITHOUT person = " + person.getName());
        }

        NList<Person> personsWithCar = NQL.search(mPerson).searchNotNull(mPerson.getCar()).getList();
        System.out.println("personsWithCar.getNumberFound() = " + personsWithCar.getNumberFound());
        for (Person person : personsWithCar) {
            System.out.println("WITH person = " + person.getName());
        }

        NList<Person> persons = NQL.search(mPerson).getList();
        System.out.println("persons.getNumberFound() = " + persons.getNumberFound());
        for (Person person : persons) {
            System.out.println("ALL person = " + person.getName());
        }


    }
}
