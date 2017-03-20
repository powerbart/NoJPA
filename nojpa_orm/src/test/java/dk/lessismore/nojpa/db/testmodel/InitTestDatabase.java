package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.reflection.db.DatabaseCreator;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class InitTestDatabase {

    private static final Logger log = LoggerFactory.getLogger(InitTestDatabase.class);

    public static void createTables() {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.db.testmodel");
    }



    private static void save(ModelObjectInterface o){
        ModelObjectService.save(o);
    }

    public static void createTestData(){
        Address address1 = ModelObjectService.create(Address.class);
        address1.setStreet("Address1");
        address1.setZip(1111);
        save(address1);




        Address address2 = ModelObjectService.create(Address.class);
        address2.setStreet("Address2");
        address2.setZip(2222);
        save(address2);

        Address address3 = ModelObjectService.create(Address.class);
        address3.setStreet("Address3");
//        address3.setZip(3333);
        save(address3);

        Car ford = ModelObjectService.create(Car.class);
        ford.setBrand("Ford");
        ford.setAddress(address1);
        save(ford);

        Car toyota = ModelObjectService.create(Car.class);
        toyota.setBrand("Toyota");
        toyota.setAddress(address2);
        save(toyota);

        List<Person> employees = new ArrayList<Person>();
        Person c = ModelObjectService.create(Person.class);
        c.setName("C.B.C");
        c.setIsSick(true);
        employees.add(c);
        c.setAddresses(new Address[]{address2});
        save(c);

        Person b =ModelObjectService.create(Person.class);
        b.setName("B.B");
        b.setChildren(new Person[]{c});
        b.setCar(toyota);
        employees.add(b);
        b.setSomeFloat(20);
        b.setAddresses(new Address[]{address1, address3});
        save(b);

        Person a = ModelObjectService.create(Person.class);
        a.setName("A");
        a.setChildren(new Person[]{b});
        a.setCar(ford);
        a.setSomeFloat(10);
        a.setAddresses(new Address[]{address1, address2});
        employees.add(a);
        save(a);

        Company planetExpress = ModelObjectService.create(Company.class);
        planetExpress.setName("Planet Express");
        planetExpress.setEmployees(employees.toArray(new Person[employees.size()]));
        planetExpress.setCeo(a);
        planetExpress.setCfo(b);
        save(planetExpress);

        Company planet2Express = ModelObjectService.create(Company.class);
        planet2Express.setName("Planet Impress");
        planet2Express.setEmployees(employees.toArray(new Person[employees.size()]));
        planet2Express.setCeo(b);
        planet2Express.setCfo(a);
        save(planet2Express);

    }


    public static void initPlanetExpress() {
        createTables();
    }



}