package dk.lessismore.nojpa.db;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.db.testmodel.Car;
import dk.lessismore.nojpa.db.testmodel.Person;
import dk.lessismore.nojpa.db.testmodel.InitTestDatabase;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import org.junit.Test;
import org.junit.BeforeClass;

import static org.junit.Assert.*;

public class ModelReferenceTest {

    @BeforeClass
    public static void initDatabase() {
        InitTestDatabase.initPlanetExpress();
        InitTestDatabase.createTestData();
    }    

    @Test
    public void getAfterSetTest() {
        Person person = ModelObjectService.create(Person.class);
        person.setName("John");

        assertNull(person.getCar());

        Car car = ModelObjectService.create(Car.class);
        car.setBrand("Ford Taunus");
        person.setCar(car);
        assertEquals(car, person.getCar());
        assertEquals(car.getBrand(), person.getCar().getBrand());

        car.setBrand("Toyota Carina");
        assertEquals(car, person.getCar());
        assertEquals(car.getBrand(), person.getCar().getBrand());

        ModelObjectService.save(person);
        ModelObjectService.save(car);

        Person person2 = MQL.selectByID(Person.class, person.getObjectID());
        person2.setCar(car);
        assertEquals(car, person2.getCar());
        assertEquals(car.getBrand(), person2.getCar().getBrand());

        car.setBrand("Citroen Berlingo");
        assertEquals(car, person2.getCar());
        assertEquals(car.getBrand(), person2.getCar().getBrand());
    }
}
