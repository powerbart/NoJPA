package dk.lessismore.nojpa.utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import dk.lessismore.nojpa.db.testmodel.Person;

/**
 * Created by IntelliJ IDEA.
 * User: seb
 * Date: 07-03-2011
 * Time: 12:14:11
 * To change this template use File | Settings | File Templates.
 */
public class GenericComparatorTest {



    @Test
     public void testLevel1() throws Exception {
        ArrayList<Person> ps = new ArrayList<Person>();
        for(int i = 0; i < 5; i++){
            Person p = ModelObjectService.create(Person.class);
            p.setName("MyName" + i);
            ps.add(p);
        }

        System.out.println("-------- BEFORE ------------");
        for(int i = 0; i < ps.size(); i++){
            System.out.println("" + ps.get(i).getName());
        }
        System.out.println("-------- AFTER 1 ------------");
        Collections.sort(ps, new GenericComparator(Person.class, "name"));
        for(int i = 0; i < ps.size(); i++){
            System.out.println("" + ps.get(i).getName());
        }

        System.out.println("-------- AFTER 2 ------------");
        Collections.sort(ps, new GenericComparator(Person.class, "name", true));
        for(int i = 0; i < ps.size(); i++){
            System.out.println("" + ps.get(i).getName());
        }
        assertEquals(1,1);
     }






}
