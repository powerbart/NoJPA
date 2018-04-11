package dk.lessismore.nojpa.masterworker.bean.worker;

import dk.lessismore.nojpa.masterworker.executor.Executor;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteBeanMessage;
import dk.lessismore.nojpa.masterworker.messages.NewRemoteBeanMessage;
import dk.lessismore.nojpa.masterworker.bean.RemoteBeanInterface;
import dk.lessismore.nojpa.guid.GuidFactory;
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
        while(!done && !isRemoteBeanClosing()) {
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
                log.error("We will close down.... - we got a closeDownRemoteBean-request");
                log.error("We will close down.... - we got a closeDownRemoteBean-request");
            } else {
                Method method = null;

                Method[] methods = objectToRunOn.getClass().getMethods();
                for(int i = 0; i < methods.length; i++){
                    if(methods[i].getName().equals(n.getMethodName())){
                        method = methods[i];
                        break;
                    }
                }
                Object returnValue = method.invoke(objectToRunOn, n.getArgs());
                final String s = String.valueOf(returnValue);
                log.debug(n.getMethodName() + " returns ( " + (returnValue == null ? null : (s.length() > 20 ? s.substring(0, 20) + "..." : s)) + ")");

                //will update progress from 0.00 to 0.99
                if(objectToRunOn.getProgress() < 0.1){
                    this.setProgress(((this.getProgress() * 100 + 1) % 100) / 100);
                }

                return returnValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Some error " + e, e);
        }
        return null;
    }
}
