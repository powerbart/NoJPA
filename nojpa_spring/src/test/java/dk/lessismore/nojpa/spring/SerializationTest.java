package dk.lessismore.nojpa.spring;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.reflection.db.DatabaseCreator;
import dk.lessismore.nojpa.rest.NoJpaMapper;
import dk.lessismore.nojpa.spring.model.Person;
import org.junit.Test;

import java.util.List;

/**
 * Created by seb on 19/1/15.
 */
public class SerializationTest {

    @Test
    public void test01() throws Exception {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
        Person mPerson = MQL.mock(Person.class);


        NoJpaMapper noJpaMapper = new NoJpaMapper();
        List<Person> list = MQL.select(mPerson).getList();

        System.out.println(noJpaMapper.writeValueAsString(list));
    }
}
