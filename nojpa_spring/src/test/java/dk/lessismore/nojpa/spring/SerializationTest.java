package dk.lessismore.nojpa.spring;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.db.methodquery.NList;
import dk.lessismore.nojpa.db.methodquery.NQL;
import dk.lessismore.nojpa.reflection.db.DatabaseCreator;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectSearchService;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import dk.lessismore.nojpa.reflection.db.model.SolrServiceImpl;
import dk.lessismore.nojpa.rest.NoJpaMapper;
import dk.lessismore.nojpa.spring.model.Car;
import dk.lessismore.nojpa.spring.model.Person;
import junit.framework.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by niakoi on 19/1/15.
 */
public class SerializationTest {

    @Test
    public void test01() throws Exception {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        SolrServiceImpl solrService = new SolrServiceImpl();
        solrService.setCoreName("nojpa");
        solrService.setCleanOnStartup(true);

        ModelObjectSearchService.addSolrServer(Person.class, solrService.getServer());

        for (int i = 0; i < 10; i++) {
            Person person = ModelObjectService.create(Person.class);
            person.setName("person " + (i % 4));
            ModelObjectService.save(person);
            ModelObjectSearchService.put(person);
        }
        solrService.commit();

        Person nPerson = NQL.mock(Person.class);
        Person mPerson = MQL.mock(Person.class);

        NList<Person> persons = NQL.search(nPerson).getList();
        Assert.assertEquals(persons.getNumberFound(), 10);

        NoJpaMapper noJpaMapper = new NoJpaMapper();
        List<Person> list = MQL.select(mPerson).getList();

        System.out.println(noJpaMapper.writeValueAsString(list));
        System.out.println(noJpaMapper.writeValueAsString(persons));
    }
}
