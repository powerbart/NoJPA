package dk.lessismore.nojpa.reflection.attributeconverters;
/**
 * This class can convert a Class to an string and back again.
 *
 * @version 1.0 21-5-2
 * @author LESS-IS-MORE ApS
 */
public class ClassConverter extends AttributeConverter {

    public Object stringToObject(String str) {
        try {
            return Class.forName(str);
        }catch(ClassNotFoundException enfe) {
        }catch(ExceptionInInitializerError eie) {
        }catch(LinkageError le){
        }
        return null;
    }

    public String objectToString(Object object) {
        return ((Class)object).getName();
    }
    protected Class getObjectClass() {
        return Class.class;
    }
}
