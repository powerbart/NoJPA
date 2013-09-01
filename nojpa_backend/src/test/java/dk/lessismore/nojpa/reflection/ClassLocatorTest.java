package dk.lessismore.nojpa.reflection;

import dk.lessismore.nojpa.db.model.Company;
import dk.lessismore.nojpa.db.model.MrRich;
import dk.lessismore.nojpa.db.model.Person;
import dk.lessismore.nojpa.db.model.Woman;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
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
