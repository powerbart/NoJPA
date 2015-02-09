package dk.lessismore.nojpa.db.statements.oracle;

import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import oracle.jdbc.OracleTypes;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.Calendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class PackageProxy implements InvocationHandler, PackageStats {

    private transient static final org.apache.log4j.Logger log = Logger.getLogger(PackageProxy.class);


    private final Class storedProcedurePackageInterface;
    private final PackageTarget packageTargetAnnotation;
    public Object proxyObject;

    public <I> PackageProxy(Class<I> storedProcedurePackageInterface) {
        this.storedProcedurePackageInterface = storedProcedurePackageInterface;
        packageTargetAnnotation = storedProcedurePackageInterface.getAnnotation(PackageTarget.class);
        if(packageTargetAnnotation == null){
            throw new RuntimeException("We can't see the annotation PackageTarget on " + storedProcedurePackageInterface.getSimpleName());
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.debug("We got the call " + storedProcedurePackageInterface.getSimpleName() + "." + method.getName() + " with args("+ args +")");
        //        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@//192.168.56.101:1521", "system", "1234");
        //CallableStatement stmt = conn.prepareCall("{ call crb_capital_cost_pkg.get_measures(?,?,?,?,?) }");
        //int parameterCount = 1;
        StringBuilder call = new StringBuilder();
        call.append("{ call " + packageTargetAnnotation.name() + "." + OracleUtil.javaNameToOracle(method.getName()) + "(?");
        for(int i = 0; args != null && i < args.length; i++){
            call.append(",?");
        }
        call.append(") }");
        log.debug("We will now call " + call + " on " + packageTargetAnnotation.dataSource());
        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@//192.168.56.101:1521", "system", "1234");
        CallableStatement stmt = conn.prepareCall(call.toString());
        int parameterCount = 1;
        stmt.registerOutParameter(parameterCount++, OracleTypes.CURSOR);
        for(int i = 0; args != null && i < args.length; i++){
            Class attributeClass = method.getParameterTypes()[i];
            if(attributeClass.isAssignableFrom(Calendar.class)){
                stmt.setTimestamp(parameterCount++, new Timestamp(((Calendar)args[i]).getTimeInMillis()));
            } else if(attributeClass.equals(Integer.TYPE)){
                stmt.setInt(parameterCount++, (Integer) args[i]);
            } else if(attributeClass.equals(Long.TYPE)){
                stmt.setLong(parameterCount++, (Long) args[i]);
            } else if(attributeClass.equals(Float.TYPE)){
                stmt.setFloat(parameterCount++, (Float) args[i]);
            } else if(attributeClass.equals(Double.TYPE)){
                stmt.setDouble(parameterCount++, (Double) args[i]);
            } else if(attributeClass.equals(Boolean.TYPE)){
                stmt.setBoolean(parameterCount++, (Boolean) args[i]);
            } else if(attributeClass.equals(String.class)){
                stmt.setString(parameterCount++, (String) args[i]);
            }
        }
        stmt.execute();

        Class<?> returnType = method.getReturnType();
        if(returnType.isAssignableFrom(List.class)){
            Type genericReturnType = method.getGenericReturnType();


        }


        System.out.println("returnType.getGenericSuperclass() = " + returnType.getGenericSuperclass());
        Type[] genericInterfaces = returnType.getGenericInterfaces();
        System.out.println("genericInterfaces.length = " + genericInterfaces.length);
        System.out.println("returnType.getGenericInterfaces() = " + genericInterfaces);
        System.out.println("method.getGenericReturnType() = " + method.getGenericReturnType());


        ResultSet rs = (ResultSet) stmt.getObject(1);
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();


        for(int i = 1; i <= columnCount; i++){
            System.out.print(metaData.getColumnName(i) + "("+ metaData.getColumnTypeName(i) +")|");
        }



        while (rs.next()) {
            System.out.println("");
            for(int i = 1; i <= columnCount; i++){
                System.out.print(rs.getString(i) + " | ");
            }
        }
        rs.close();
        stmt.close();






        return null;
    }




}
