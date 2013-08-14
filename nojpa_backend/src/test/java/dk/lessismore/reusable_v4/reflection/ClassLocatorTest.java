package dk.lessismore.reusable_v4.reflection;

import dk.lessismore.reusable_v4.db.model.Company;
import dk.lessismore.reusable_v4.db.model.MrRich;
import dk.lessismore.reusable_v4.db.model.Person;
import dk.lessismore.reusable_v4.db.model.Woman;
import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectInterface;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ClassLocatorTest {

    @Test
    public void testGetClassesInPackageOfInterface() throws ClassNotFoundException {
        List<Class<? extends ModelObjectInterface>> modelClasses =
                ClassLocator.getClassesInPackageOfInterface(Person.class.getPackage(), ModelObjectInterface.class);
        assertTrue(modelClasses.containsAll(Arrays.asList(Person.class, Company.class, MrRich.class)));
        assertTrue(modelClasses.containsAll(Arrays.asList(Woman.class)));
    }
}
