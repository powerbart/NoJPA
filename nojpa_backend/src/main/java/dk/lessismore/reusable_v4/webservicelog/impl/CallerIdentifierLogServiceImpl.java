package dk.lessismore.reusable_v4.webservicelog.impl;

import dk.lessismore.reusable_v4.reflection.db.model.ModelObjectProxy;
import dk.lessismore.reusable_v4.webservicelog.CallerIdentifierLogService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import dk.lessismore.reusable_v4.webservicelog.CallerIdentifierLog;

/**
 * Created by IntelliJ IDEA.
 * User: Sebastian Berg, seb@exiqon.com
 * Date: 26-06-2008
 * Time: 03:33:36
 */
public class CallerIdentifierLogServiceImpl implements CallerIdentifierLogService, MethodInterceptor {

   public Object invoke(MethodInvocation i) throws Throwable {
       try{
         Object ret = i.proceed();
         return ret;
       } catch (Throwable t){           
           t.printStackTrace();
           throw t;
       }
   }

    public CallerIdentifierLog newLocalInstance(String methodName) {
        final CallerIdentifierLog caller = ModelObjectProxy.create(CallerIdentifierLog.class);
        caller.setSystemUserName("coreone-local@exiqon.com");
        caller.setCallerApplicationName("coreone");
        caller.setCallerMethodName( methodName );
        return caller;
    }

    public CallerIdentifierLog newInstance(String loginUserName, String systemUserName, String callerApplicationName, String callerMethodName, String callerHostName, String callerVersionName) {
        final CallerIdentifierLog caller = ModelObjectProxy.create(CallerIdentifierLog.class);
        caller.setLoginUserName(loginUserName);
        caller.setSystemUserName(systemUserName);
        caller.setCallerApplicationName( callerApplicationName );
        caller.setCallerMethodName( callerMethodName );
        caller.setCallerHostName( callerHostName );
        caller.setCallerVersion( callerVersionName );
        return caller;
    }
}
