package dk.lessismore.nojpa.masterworker.bean.worker;

import dk.lessismore.nojpa.masterworker.executor.Executor;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteBeanMessage;
import dk.lessismore.nojpa.masterworker.messages.NewRemoteBeanMessage;
import dk.lessismore.nojpa.masterworker.bean.RemoteBeanInterface;
import dk.lessismore.nojpa.guid.GuidFactory;
import dk.lessismore.nojpa.reflection.db.DbObjectSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 */
public class BeanExecutor extends Executor<NewRemoteBeanMessage, Object> {

    private static final Logger log = LoggerFactory.getLogger(BeanExecutor.class);

    private boolean done = false;
    private RemoteBeanInterface objectToRunOn = null;
    private Class<? extends RemoteBeanInterface> remoteBeanClass;

    public BeanExecutor(Class<? extends RemoteBeanInterface> remoteBeanClass, RemoteBeanInterface objectToRunOn) {
        this.objectToRunOn = objectToRunOn;
        this.remoteBeanClass = remoteBeanClass;
    }


    public Object run(NewRemoteBeanMessage n) {

//        try {
//            // TODO if implClassName is set, use that class
////            objectToRunOn = (RemoteBeanInterface) Class.forName(implClassName).newInstance();
//        } catch (InstantiationException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }


        //TODO: Do we need this....?
        while(!done) {
            log.debug("Waiting for RemoteMethod: " + n + " " + super.toString());
            try {
                Thread.sleep(5_000);
                this.setProgress( objectToRunOn.getProgress() );
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return "-me-done-running-";
    }


    public Object runMethod(RunMethodRemoteBeanMessage n) {
        log.debug("Now running method " + n.getJobID() + " / " + n.getMethodName() + " " + GuidFactory.getInstance().makeGuid() + " " + System.currentTimeMillis() + " " + super.toString());
        try {
            if(n.getMethodName().equals("closeDownRemoteBean")){
                done = true;
                log.info("We will close down....");
                Thread.sleep(2000);
                System.exit(0);
            } else {
                Method method = null;

                Method[] methods = objectToRunOn.getClass().getMethods();
                for(int i = 0; i < methods.length; i++){
                    if(methods[i].getName().equals(n.getMethodName())){
                        method = methods[i];
                        break;
                    }
                }
//                objectToRunOn.getClass().getMethod(n.getMethodName(), findArgsClasses(n.getArgs()));
                Object returnValue = method.invoke(objectToRunOn, n.getArgs());
                log.debug(n.getMethodName() + " returns ( " + returnValue + ")");
                return returnValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Some error " + e, e);
        }
        return null;
    }

//    private static Class[] findArgsClasses(Object[] objects){
//        if(objects == null || objects.length == 0) return null;
//        Class[] classes = new Class[objects.length];
//        for(int i = 0; i < objects.length; i++){
//            classes[i] = objects[i].getClass();
//        }
//        return classes;
//    }
//
}
