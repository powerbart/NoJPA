package dk.lessismore.nojpa.db.statements.oracle;


import java.lang.reflect.Proxy;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class OracleDB {


    public static <I> I connectInterface(Class<I> storedProcedurePackageInterface) {
        PackageProxy object = new PackageProxy(storedProcedurePackageInterface);
        object.proxyObject = Proxy.newProxyInstance(
                storedProcedurePackageInterface.getClassLoader(),
                new Class<?>[] { storedProcedurePackageInterface, PackageStats.class },
                object);
        I toReturn = (I) object.proxyObject;
        return toReturn;
    }





}
