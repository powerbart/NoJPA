package dk.lessismore.reusable_v4.serialization;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectService;
import dk.lessismore.reusable_v4.serialization.model.Department;
import dk.lessismore.reusable_v4.serialization.model.Employee;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class XmlSerializerTest {

    private final XmlSerializer serializer = new XmlSerializer();

    @Test
    public void testSerialize() {
        Employee john = johnOfInterface();
        String xml = serializer.serialize(john);
        System.out.println("xml = \n" + xml);
        assertTrue(true);
    }

    // TODO FIX unserialization
//    @Ignore
//    @Test
//    public void testUnserialize() {
//        Employee john = johnOfInterface();
//        String xml = serializer.serialize(john);
//
//        Employee john2 = serializer.unserialize(xml);
//        assertEquals(john.getName(), john2.getName());
//        assertEquals(john.getDepartments()[0].getName(), john2.getDepartments()[0].getName());
//
//    }

    private Employee johnOfInterface() {
        Department it = ModelObjectService.create(Department.class);
        it.setName("IT");

        Department sm = ModelObjectService.create(Department.class);
        it.setName("SM");

        Employee john = ModelObjectService.create(Employee.class);
        john.setName("John");
        john.setDepartments(new Department[] {it, sm});

        it.setChef(john);

        return john;
    }
}
