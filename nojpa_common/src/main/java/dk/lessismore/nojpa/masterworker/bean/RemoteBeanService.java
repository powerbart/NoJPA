package dk.lessismore.nojpa.masterworker.bean;

import dk.lessismore.nojpa.masterworker.JobStatus;
import dk.lessismore.nojpa.masterworker.bean.client.JobHandle;
import dk.lessismore.nojpa.masterworker.bean.client.JobListener;
import dk.lessismore.nojpa.masterworker.bean.client.MasterService;
import dk.lessismore.nojpa.masterworker.bean.worker.BeanExecutor;
import dk.lessismore.nojpa.masterworker.messages.NewRemoteBeanMessage;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteBeanMessage;
import dk.lessismore.nojpa.masterworker.messages.RunMethodRemoteResultMessage;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 21-10-2010
 * Time: 14:00:49
 */
public class RemoteBeanService implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(RemoteBeanService.class);


    public static <I extends RemoteBeanInterface, E extends BeanExecutor> I newRemoteBean(Class<I> beanInterface, Class<E> beanExecutor) {
        return (I) Proxy.newProxyInstance(
                beanInterface.getClassLoader(),
                new Class[] {beanInterface},
                new RemoteBeanService(beanInterface, beanExecutor));
    }

    


    private final Class<? extends ModelObjectInterface> sourceClass;
    private JobListener<Object> jobListener = null;
    JobHandle<Object> jobHandle = null;

    public RemoteBeanService(Class sourceClass, Class executorClass) {
        this.sourceClass = sourceClass;

        jobListener = new JobListener<Object>() {
             public void onStatus(JobStatus status) {
                 System.out.println("RemoteBeanService.JobListener.onStatus: "+status);
             }

             public void onProgress(double progress) {
                 System.out.println("RemoteBeanService.JobListener.onProgress: "+progress);
             }

             public void onResult(Object result) {
                 System.out.println("RemoteBeanService.JobListener.onResult: "+result);
             }

             public void onException(RuntimeException e) {
                 System.out.println("RemoteBeanService.JobListener.onException: "+e.getMessage());
             }

            public void onRunMethodRemoteResult(RunMethodRemoteResultMessage runMethodRemoteResultMessage) {
                System.out.println("RemoteBeanService.JobListener.onRunMethodRemoteResult: "+runMethodRemoteResultMessage);
            }


         };

         //JobHandle<String> jobHandle = MasterService.runJob(ToUpperExecutor.class, "WeAreNowUsingTheHandler");
         jobHandle = MasterService.runJob(executorClass, new NewRemoteBeanMessage(sourceClass));
         jobHandle.addJobListener(jobListener);
//        jobHandle.getResult();
    }


    boolean closeDownRemoteBean = false;

    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if(!closeDownRemoteBean){
            String methodName = method.getName();
            if(methodName.equals("closeDownRemoteBean")){
                closeDownRemoteBean = true;
            }
            log.debug("Now calling " + methodName);
            Object toReturn = jobHandle.runMethodRemote(new RunMethodRemoteBeanMessage(methodName,  objects));
            return toReturn;
        } else {
            throw new Exception("Remote bean is already closed!!! So you cant call " + method.getName() + " after calling closeDownRemoteBean()");
        }
    }




}
