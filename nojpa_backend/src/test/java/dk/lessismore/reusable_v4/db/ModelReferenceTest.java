package dk.lessismore.reusable_v4.db;

import dk.lessismore.reusable_v4.db.methodquery.MQ;
import dk.lessismore.reusable_v4.db.testmodel.Car;
import dk.lessismore.reusable_v4.db.testmodel.Person;
import dk.lessismore.reusable_v4.db.testmodel.InitTestDatabase;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectService;
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

        Person person2 = MQ.selectByID(Person.class, person.getObjectID());
        person2.setCar(car);
        assertEquals(car, person2.getCar());
        assertEquals(car.getBrand(), person2.getCar().getBrand());

        car.setBrand("Citroen Berlingo");
        assertEquals(car, person2.getCar());
        assertEquals(car.getBrand(), person2.getCar().getBrand());
    }
}
