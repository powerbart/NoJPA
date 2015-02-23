package dk.lessismore.nojpa.db.statements.oracle;

import dk.lessismore.nojpa.db.connectionpool.ConnectionPoolFactory;
import dk.lessismore.nojpa.reflection.db.model.ModelObject;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import oracle.jdbc.OracleTypes;
import org.apache.log4j.Logger;

import java.lang.reflect.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
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
        try {
            log.debug("We got the call " + storedProcedurePackageInterface.getSimpleName() + "." + method.getName() + " with args(" + args + ")");
            //        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@//192.168.56.101:1521", "system", "1234");
            //CallableStatement stmt = conn.prepareCall("{ call crb_capital_cost_pkg.get_measures(?,?,?,?,?) }");
            //int parameterCount = 1;
            StringBuilder call = new StringBuilder();
            call.append("{ call " + packageTargetAnnotation.name() + "." + OracleUtil.javaNameToOracle(method.getName()) + "(?");
            for (int i = 0; args != null && i < args.length; i++) {
                call.append(",?");
            }
            call.append(") }");
            log.debug("We will now call " + call + " on " + packageTargetAnnotation.dataSource());
//        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@//192.168.56.101:1521", "system", "1234");
            Connection conn = (Connection) ConnectionPoolFactory.getPool(packageTargetAnnotation.dataSource()).getFromPool();
            CallableStatement stmt = conn.prepareCall(call.toString());
            int parameterCount = 1;
            stmt.registerOutParameter(parameterCount++, OracleTypes.CURSOR);
            for (int i = 0; args != null && i < args.length; i++) {
                Class attributeClass = method.getParameterTypes()[i];
                if (attributeClass.isAssignableFrom(Calendar.class)) {
                    stmt.setTimestamp(parameterCount++, new Timestamp(((Calendar) args[i]).getTimeInMillis()));
                } else if (attributeClass.equals(Integer.TYPE)) {
                    stmt.setInt(parameterCount++, (Integer) args[i]);
                } else if (attributeClass.equals(Long.TYPE)) {
                    stmt.setLong(parameterCount++, (Long) args[i]);
                } else if (attributeClass.equals(Float.TYPE)) {
                    stmt.setFloat(parameterCount++, (Float) args[i]);
                } else if (attributeClass.equals(Double.TYPE)) {
                    stmt.setDouble(parameterCount++, (Double) args[i]);
                } else if (attributeClass.equals(Boolean.TYPE)) {
                    stmt.setBoolean(parameterCount++, (Boolean) args[i]);
                } else if (attributeClass.equals(String.class)) {
                    stmt.setString(parameterCount++, (String) args[i]);
                }
            }
            stmt.execute();

            Class<?> returnType = method.getReturnType();
            if (returnType.isAssignableFrom(List.class)) {      //TODO: Make also iterator
                Type genericReturnType = method.getGenericReturnType();
                if (genericReturnType instanceof ParameterizedType) {
                    ParameterizedType p = (ParameterizedType) genericReturnType;
                    Type type = p.getActualTypeArguments()[0];
                    if (type.toString().contains("interface")) {
                        Class c = (Class) type;
                        List toReturn = new ArrayList();
                        ResultSet rs = (ResultSet) stmt.getObject(1);
                        ResultSetMetaData metaData = rs.getMetaData();
                        while (rs.next()) {
                            Object row = OracleDataProxy.create(c);
                            log.debug("Loop - row : row = " + row);
                            ((OracleDataProxyInterface) row).readRow(rs, metaData, row);
                            toReturn.add(row);
                        }
                        rs.close();
                        stmt.close();
                        return toReturn;
                    } else if (type.toString().contains("String")) {
                        List toReturn = new ArrayList();
                        ResultSet rs = (ResultSet) stmt.getObject(1);
                        while (rs.next()) {
                            toReturn.add(rs.getString(1));
                        }
                        rs.close();
                        stmt.close();
                        return toReturn;
                    } //TODO: Fill primitive object types

                }
            } else if(returnType.isInterface()) {
                Object result = null;
                ResultSet rs = (ResultSet) stmt.getObject(1);
                ResultSetMetaData metaData = rs.getMetaData();
                if (rs.next()) {
                    result = OracleDataProxy.create(returnType);
                    log.debug("Single row : row = " + result);
                    ((OracleDataProxyInterface) result).readRow(rs, metaData, result);
                }
                rs.close();
                stmt.close();
                return result;
            } else if (returnType.isAssignableFrom(String.class)) {
                ResultSet rs = (ResultSet) stmt.getObject(1);
                String result = null;
                if (rs.next()) {
                    result = rs.getString(1);
                }
                rs.close();
                stmt.close();
                return result;
            } else if (returnType.isAssignableFrom(Calendar.class)) {
                ResultSet rs = (ResultSet) stmt.getObject(1);
                Timestamp result = null;
                if (rs.next()) {
                    result = rs.getTimestamp(1);
                }
                Calendar toReturn = null;
                if(result != null){
                    toReturn = Calendar.getInstance();
                    toReturn.setTimeInMillis(result.getTime());
                }
                rs.close();
                stmt.close();
                return toReturn;
            }  //TODO: Fill primetive return types
//
//
//
//        System.out.println("returnType.getGenericSuperclass() = " + returnType.getGenericSuperclass());
//        Type[] genericInterfaces = returnType.getGenericInterfaces();
//        System.out.println("genericInterfaces.length = " + genericInterfaces.length);
//        System.out.println("returnType.getGenericInterfaces() = " + genericInterfaces);
//        Type genericReturnType = method.getGenericReturnType();
//        System.out.println("method.getGenericReturnType() = " + genericReturnType);
//        System.out.println("method.getGenericReturnType().getClass() = " + genericReturnType.getClass());
//
//
//        ResultSet rs = (ResultSet) stmt.getObject(1);
//        ResultSetMetaData metaData = rs.getMetaData();
//        int columnCount = metaData.getColumnCount();
//
//
//        for(int i = 1; i <= columnCount; i++){
//            System.out.print(metaData.getColumnName(i) + "("+ metaData.getColumnTypeName(i) +")|");
//        }
//
//
//
//        while (rs.next()) {
//            System.out.println("");
//            for(int i = 1; i <= columnCount; i++){
//                System.out.print(rs.getString(i) + " | ");
//            }
//        }
//        rs.close();
//        stmt.close();
//
//


            return null;
        } catch (Throwable t){
            log.error("Some error when calling invoke " + t, t);
            throw t;
        }
    }




}
