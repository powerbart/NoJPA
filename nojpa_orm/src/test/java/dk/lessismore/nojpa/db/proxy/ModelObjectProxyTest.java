package dk.lessismore.nojpa.db.proxy;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.db.model.MrRich;
import dk.lessismore.nojpa.reflection.db.model.*;
import dk.lessismore.nojpa.reflection.db.DatabaseCreator;

import static dk.lessismore.nojpa.db.methodquery.MQL.Comp.*;
import dk.lessismore.nojpa.db.statements.SQLStatement;
import dk.lessismore.nojpa.db.SQLStatementExecutor;
import dk.lessismore.nojpa.db.model.Company;
import dk.lessismore.nojpa.db.model.Person;

import java.util.Calendar;
import java.util.List;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.BeforeClass;

import static org.junit.Assert.*;

public class ModelObjectProxyTest {

    private static final Logger log = Logger.getLogger(ModelObjectProxyTest.class);
    @Test
    public void dummyTest() {
        assertTrue(true);
    }

    private static void save(ModelObjectInterface o){
        ModelObjectService.save(o);
    }




    @BeforeClass
    public static void setUp() {
        List<SQLStatement> tables = new LinkedList<SQLStatement>();

        tables.addAll(DatabaseCreator.makeTableFromClass(Person.class));
        tables.addAll(DatabaseCreator.makeTableFromClass(Company.class));
        tables.addAll(DatabaseCreator.makeTableFromClass(MrRich.class));


        for(SQLStatement sqlStatement: tables) {
            String sql = sqlStatement.makeStatement();
            SQLStatementExecutor.doUpdate(sql);
        }
    }

    @Test
    public void testClassInterface() {
        Person person = ModelObjectProxy.create(Person.class);
        assertEquals(Person.class, ((ModelObject)person).getInterface());
    }

    @Test
    public void testInt() {
        MrRich mrRich = ModelObjectProxy.create(MrRich.class);
        assertTrue(mrRich.getMyInt() == 0);
        mrRich.setMyInt(42);
        assertTrue(mrRich.getMyInt() == 42);
    }

    @Test
    public void testDouble() {
        MrRich mrRich = ModelObjectProxy.create(MrRich.class);
        assertTrue(mrRich.getMyDouble() == 0);
        mrRich.setMyDouble(42);
        assertTrue(mrRich.getMyDouble() == 42);
    }

    @Test
    public void testFloat() {
        MrRich mrRich = ModelObjectProxy.create(MrRich.class);
        assertTrue(mrRich.getMyFloat() == 0);
        mrRich.setMyFloat(42);
        assertTrue(mrRich.getMyFloat() == 42);
    }

    @Test
    public void testBoolean() {
        MrRich mrRich = ModelObjectProxy.create(MrRich.class);
        assertTrue(!mrRich.getMyBoolean());
        mrRich.setMyBoolean(true);
        assertTrue(mrRich.getMyBoolean());
    }

    @Test
    public void testCalendar() {
        MrRich mrRich = ModelObjectProxy.create(MrRich.class);
        assertNull(mrRich.getCalendar());
        Calendar c = Calendar.getInstance();
        mrRich.setCalendar(c);
        assertEquals(c, mrRich.getCalendar());
    }

    @Test
    public void testMrRich() {
        MrRich mrRich = ModelObjectProxy.create(MrRich.class);
        assertNull(mrRich.getMrRich());
        MrRich mrRich2 = ModelObjectProxy.create(MrRich.class);
        mrRich.setMrRich(mrRich2);
        assertEquals(mrRich2, mrRich.getMrRich());
    }

//    @Test
//    @Ignore
//    public void testMrRichArray() {
//        MrRich mrRich = ModelObjectProxy.create(MrRich.class);
//        assertNull(mrRich.getMrRichArray());
//        MrRich[] mrRichArray = new MrRich[] {ModelObjectProxy.create(MrRich.class)};
//        mrRich.setMrRichArray(mrRichArray);
//        assertArrayEquals(mrRichArray, mrRich.getMrRichArray());
//    }

    @Test
    public void testSimpleCreation() {
        Person person = ModelObjectProxy.create(Person.class);
        person.setName("King Kong");
        person.setAge(1958);
        assertEquals("King Kong", person.getName());
        save(person);

        Person personMock = MQL.mock(Person.class);
        Person person2 = MQL.select(personMock).where(personMock.getName(), EQUAL, "King Kong").getFirst();
        assertEquals("King Kong", person2.getName());
    }
//TODO: Add
//    @Test
//    public void testCreation() {
//        Person[] employees = new Person[2];
//
//        employees[0] = ModelObjectProxy.create(Person.class);
//        employees[0].setName("Peter Naur");
//        employees[0].setAge(80);
//
//        employees[1] = ModelObjectProxy.create(Person.class);
//        employees[1].setName("Donald Knuth");
//        employees[1].setAge(71);
//
//        Company company = ModelObjectProxy.create(Company.class);
//        company.setName("Ajax");
//        company.setEmployees(employees);
//        assertNotNull(company.getEmployees());
//
//        ModelObjectService.save(company);
//
//        assertEquals("Ajax", company.getName());
//
//        Person mock = MQ.mock(Person.class);
//
//        Person person1 = MQ.select(mock).where(mock.getAge(), EQUAL, 80).getFirst();
//        assertEquals("Peter Naur", person1.getName());
//
//        Person person2 = MQ.select(mock).where(mock.getAge(), EQUAL, 71).getFirst();
//        assertEquals("Donald Knuth", person2.getName());
//
//        Company loadedCompany = MQ.select(Company.class).getFirst();
//        assertNotNull(loadedCompany);
//
//        Person[] persons = loadedCompany.getEmployees();
//        assertNotNull(persons);
//        assertEquals(2, persons.length);
//
//        if(persons[0].getAge() == 80) {
//            assertEquals("Peter Naur", persons[0].getName());
//            assertEquals("Donald Knuth", persons[1].getName());
//        } else {
//            assertEquals("Donald Knuth", persons[0].getName());
//            assertEquals("Peter Naur", persons[1].getName());
//        }
//    }

}
