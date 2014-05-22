package dk.lessismore.nojpa.spring;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import dk.lessismore.nojpa.reflection.db.DatabaseCreator;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectService;
import dk.lessismore.nojpa.rest.NoJpaMapper;
import dk.lessismore.nojpa.spring.model.Car;
import dk.lessismore.nojpa.spring.model.Company;
import dk.lessismore.nojpa.spring.model.Person;
import org.junit.Test;

/**
 * Created by niakoi on 22/5/14.
 */
public class MQSerializeTest {

    public static void main(String[] args) throws Exception {
        DatabaseCreator.createDatabase("dk.lessismore.nojpa.spring");
        Company company = ModelObjectService.create(Company.class);
        company.setName("lessismore");

        Person ceo = ModelObjectService.create(Person.class);
        ceo.setName("ceo");
        ceo.setCar(getCar("ceo_car"));
        ceo.setCars(new Car[]{getCar("aux_car1"), getCar("aux_car2")});

        Person dev1 = ModelObjectService.create(Person.class);
        dev1.setName("dev1");
        dev1.setCar(getCar("dev1_car"));

        Person dev2 = ModelObjectService.create(Person.class);
        dev2.setName("dev2");



        company.setCeo(ceo);
        company.setPersons(new Person[] {ceo, dev1, dev2});
        NoJpaMapper noJpaMapper = new NoJpaMapper();
        System.out.println(noJpaMapper.writeValueAsString(company));
        System.out.println(noJpaMapper.writeValueAsString(ceo));
        System.out.println(noJpaMapper.writeValueAsString(dev1));
        System.out.println(noJpaMapper.writeValueAsString(dev2));
    }

    static Car getCar(String name) {
        Car car = ModelObjectService.create(Car.class);
        car.setName(name);
        return car;
    }
}
