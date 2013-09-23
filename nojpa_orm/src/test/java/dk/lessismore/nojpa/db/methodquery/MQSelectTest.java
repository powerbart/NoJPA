package dk.lessismore.nojpa.db.methodquery;

import dk.lessismore.nojpa.db.testmodel.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static dk.lessismore.nojpa.db.methodquery.MQL.*;
import static dk.lessismore.nojpa.db.methodquery.MQL.Comp.EQUAL;
import static dk.lessismore.nojpa.db.methodquery.MQL.Comp.EQUAL_OR_GREATER;
import static dk.lessismore.nojpa.db.methodquery.MQL.Comp.LIKE;
import static dk.lessismore.nojpa.db.methodquery.MQL.Order.ASC;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;


public class MQSelectTest {

    private static final Company companyMock = MQL.mock(Company.class);
    private static final Person personMock = MQL.mock(Person.class);
    private static final Car carMock = MQL.mock(Car.class);
    private static final Address addressMock = MQL.mock(Address.class);

    private static Car ford;
    private static Car toyota;
    private static Person a;
    private static Person ab;
    private static Person abc;

    @BeforeClass
    public static void initDatabase() {
        InitTestDatabase.initPlanetExpress();
        InitTestDatabase.createTestData();
        ford = MQL.select(carMock).where(carMock.getBrand(), EQUAL, "Ford").getFirst();
        toyota = MQL.select(carMock).where(carMock.getBrand(), EQUAL, "Toyota").getFirst();
        a = MQL.select(personMock).where(personMock.getName(), EQUAL, "A").getFirst();
        ab = MQL.select(personMock).where(personMock.getName(), EQUAL, "A.B").getFirst();
        abc = MQL.select(personMock).where(personMock.getName(), EQUAL, "A.B.C").getFirst();
    }

    //TODO: SEB
//    @Test
//    public void testSelectHasAndOr() throws Exception {
//
//        Constraint fordDriver = has(personMock.getCar().getBrand(), EQUAL, "Ford");
//        Constraint toyotaDriver = has(personMock.getCar().getBrand(), EQUAL, "Toyota");
//        Constraint zip1111 = has(personMock.getAddresses()[ANY].getZip(), EQUAL, 1111);
//        Constraint zip2222 = has(personMock.getAddresses()[ANY].getZip(), EQUAL, 2222);
//
//        Person[] employees = MQ.select(personMock)
//                .where(all(fordDriver, toyotaDriver))
//                .orderBy(personMock.getName(), ASC)
//                .getArray();
//        assertArrayEquals(new Person[]{}, employees);
//
//        employees = MQ.select(personMock)
//                .where(fordDriver)
//                .where(toyotaDriver)
//                .orderBy(personMock.getName(), ASC)
//                .getArray();
//        assertArrayEquals(new Person[]{}, employees);
//
//        employees = MQ.select(personMock)
//                .where(any(fordDriver, toyotaDriver))
//                .orderBy(personMock.getCar().getBrand(), ASC)
//                .getArray();
//        assertEquals(2, employees.length);
//        assertEquals("Ford", employees[0].getCar().getBrand());
//        assertEquals("Toyota", employees[1].getCar().getBrand());
//
//        employees = MQ.select(personMock)
//                .where(any(
//                        all(zip1111, fordDriver),
//                        all(zip2222, toyotaDriver)))
//                .orderBy(personMock.getCar().getBrand(), ASC)
//                .getArray();
//        assertArrayEquals(new Person[] {a}, employees);
//
//        employees = MQ.select(personMock)
//                .where(all(
//                        any(zip1111, fordDriver),
//                        any(zip2222, toyotaDriver)))
//                .orderBy(personMock.getCar().getBrand(), ASC)
//                .getArray();
//        assertArrayEquals(new Person[] {a, ab}, employees);
//    }

    @Test
    public void testSelectMockJoin() throws Exception {
        Person personMock = MQL.mock(Person.class);
        Car carMock = MQL.mock(Car.class);

        Car[] cars = MQL.select(carMock)
                .where(personMock.getCar(), EQUAL, carMock) // This is the explicit mock join
                .where(personMock.getCar().getBrand(), LIKE, "%o%")
                .where(personMock.getName(), EQUAL, "A")
                .orderBy(carMock.getBrand(), ASC)
                .getArray();
        assertArrayEquals(new Car[] {ford}, cars);
    }

    @Test
    public void testSumJoin() throws Exception {
        Person personMock = MQL.mock(Person.class);
        MQL.select(personMock).getSum(personMock.getCar().getVolume());
    }

    @Test
    public void testSum() throws Exception {
        Person personMock = MQL.mock(Person.class);
        MQL.select(personMock).getSum(personMock.getCountOfCars());
    }

    @Test
    public void testSumWithWhere() throws Exception {
        Person personMock = MQL.mock(Person.class);
        MQL.select(personMock).where(personMock.getCreationDate(), EQUAL_OR_GREATER, Calendar.getInstance()).getSum(personMock.getCountOfCars());
    }

    @Test
    public void testSumWithJoinWhere() throws Exception {
        Person personMock = MQL.mock(Person.class);
        MQL.select(personMock).where(personMock.getCar().getCreationDate(), EQUAL_OR_GREATER, Calendar.getInstance()).getSum(personMock.getCountOfCars());
    }

    @Test
    public void testSumWithJoinWhere2() throws Exception {
        Person personMock = MQL.mock(Person.class);
        MQL.select(personMock).where(personMock.getCar().getCreationDate(), EQUAL_OR_GREATER, Calendar.getInstance()).getSum(personMock.getCar().getVolume());
    }

    @Test
    public void test2Joins() throws Exception {
        Person personMock = MQL.mock(Person.class);
        MQL.select(personMock)
                .where(personMock.getCar().getCreationDate(), EQUAL_OR_GREATER, Calendar.getInstance())
                .where(personMock.getCar().getBrand(), EQUAL, "Ford")
                .where(personMock.getCar().getVolume(), EQUAL_OR_GREATER, 2d).getList();


    }

    //TODO: SEB
//    @Test
//    public void testSelectSimple() throws Exception {
//        // Simple test
//        Company planetExpress = MQ.select(companyMock).getFirst();
//        assertEquals("Planet Express", planetExpress.getName());
//
//        // Test order by ASC
//        Person[] employees = MQ.select(personMock).orderBy(personMock.getName(), ASC).getArray();
//        assertArrayEquals(new Person[] {a, ab, abc}, employees);
//
//        // Test order by DESC
//        employees = MQ.select(personMock).orderBy(personMock.getName(), DESC).getArray();
//        assertArrayEquals(new Person[] {abc, ab, a}, employees);
//
//        // Test order by model not mentioned in constraint
//        employees = MQ.select(personMock).orderBy(personMock.getCar().getBrand(), ASC).getArray();
//        assertArrayEquals(new Person[] {a, ab}, employees); // Notice persons without a car is omitted
//
//        // Test order by model not mentioned in constraint REVERSED
//        employees = MQ.select(personMock).orderBy(personMock.getCar().getBrand(), DESC).getArray();
//        assertArrayEquals(new Person[] {ab, a}, employees);
//
//        // Test mock auto joiner - String in the end
//        employees = MQ.select(personMock).where(personMock.getCar().getBrand(), EQUAL, "Ford").orderBy(personMock.getName(), ASC).getArray();
//        assertArrayEquals(new Person[] {a}, employees);
//
//        Car car = MQ.select(carMock).where(carMock.getBrand(), EQUAL, "Ford").getFirst();
//        assertEquals(ford, car);
//
//        // Test mock auto joiner - ModelClass in the end - explicit use of getObjectID
//        employees = MQ.select(personMock).where(personMock.getCar().getObjectID(), EQUAL, ford.getObjectID()).getArray();
//        assertArrayEquals(new Person[] {a}, employees);
//
//        // Test mock auto joiner - ModelClass in the end - implicit use of getObjectID
//        employees = MQ.select(personMock).where(personMock.getCar(), EQUAL, ford).getArray();
//        for(Person p: employees) {
//            System.out.println("p.getName() = " + p.getName());
//        }
//        assertArrayEquals(new Person[] {a}, employees);
//
//        // Test mock auto joiner - ModelClass in the end - explicit use of getObjectID
//        employees = MQ.select(personMock).where(personMock.getCar().getObjectID(), EQUAL, ford.getObjectID()).orderBy(personMock.getName(), ASC).getArray();
//        assertArrayEquals(new Person[] {a}, employees);
//    }

    @Test
    public void testSelectANY2() throws Exception {
        Company company = MQL.mock(Company.class);
        List<Company> companies = MQL.select(company).where(company.getDescription(), Comp.EQUAL, "Hej").getList();
        for(int i = 0; i < companies.size(); i++){
            System.out.println("companies.get(i).getName() = " + companies.get(i).getName());
        }
    }



    @Test
    public void testSelectANY() throws Exception {
        // Test ANY
        Company planetExpress = MQL
                .select(companyMock)
                .where(companyMock.getEmployees()[ANY].getCar(), EQUAL, ford)
                .getFirst();
        assertEquals("Planet Express", planetExpress.getName());

        // Test ANY - more joins
        planetExpress = MQL
                .select(companyMock)
                .where(companyMock.getEmployees()[ANY].getCar().getBrand(), EQUAL, "Ford")
                .getFirst();
        assertEquals("Planet Express", planetExpress.getName());

        Address address1 = MQL.select(addressMock).where(addressMock.getStreet() , EQUAL, "Address1").getFirst();
        assertEquals("Address1", address1.getStreet());

        // Test ANY - More ANY's
        planetExpress = MQL
                .select(companyMock)
                .where(companyMock.getEmployees()[ANY].getAddresses()[ANY].getStreet(), EQUAL, "Address1")
                .getFirst();
        assertEquals("Planet Express", planetExpress.getName());

        // Test ANY - Array as return type of last mock call
        planetExpress = MQL
                .select(companyMock)
                .where(companyMock.getEmployees()[ANY].getAddresses()[ANY], EQUAL, address1)
                .getFirst();
        assertEquals("Planet Express", planetExpress.getName());

        // Test MORE WHEREs
        planetExpress = MQL
                .select(companyMock)
                .where(companyMock.getEmployees()[ANY].getCar(), EQUAL, ford)
                .where(companyMock.getEmployees()[ANY].getAddresses()[ANY], EQUAL, address1)
                .where(companyMock.getEmployees()[ANY].getAddresses()[ANY].getStreet(), EQUAL, "Address1")
                .where(companyMock.getEmployees()[ANY].getName(), EQUAL, "A")
                .getFirst();
        assertEquals("Planet Express", planetExpress.getName());

        // Test MORE WHEREs - no result
        planetExpress = MQL
                .select(companyMock)
                .where(companyMock.getEmployees()[ANY].getCar(), EQUAL, ford)
                .where(companyMock.getEmployees()[ANY].getAddresses()[ANY], EQUAL, address1)
                .where(companyMock.getEmployees()[ANY].getAddresses()[ANY].getStreet(), EQUAL, "Address1")
                .where(companyMock.getEmployees()[ANY].getName(), EQUAL, "None")
                .getFirst();
        assertNull(planetExpress);
    }

    
    @Test
    public void testDefault() throws Exception {
        BigAttObject bigAttObject = ModelObjectService.create(BigAttObject.class);
        assertEquals(bigAttObject.getMyBoolean1(), true);
        assertEquals(bigAttObject.getMyBoolean2(), false);
        assertEquals(bigAttObject.getMyBoolean3(), false);
        assertEquals(bigAttObject.getMyInt0(), 0);
        assertEquals(bigAttObject.getMyInt666(), 666);
        assertEquals(bigAttObject.getMyLong0(), 0);
        assertEquals(bigAttObject.getMyLong666(), 666);
        assertEquals(bigAttObject.getMyFloat0(), 0f, 0.1);
        assertEquals(bigAttObject.getMyFloat1(), 1f, 0.1);
        assertEquals(bigAttObject.getStringNull(), null);

        System.out.println("String.class.isPrimitive() = " + String.class.isPrimitive());

        assertEquals(bigAttObject.getStringSeb(), "Seb");
        assertEquals(bigAttObject.getCalendarNull(), null);
        assertNotNull(bigAttObject.getCalendarNow());

    }
    
    

}