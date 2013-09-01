package dk.lessismore.nojpa.reflection.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by IntelliJ IDEA.
 * User: seb
 * Date: 11-07-11
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelObjectLifeCycleListener {



    public Class<? extends LifeCycleListener> lifeCycleListener();

    public static interface LifeCycleListener {

        public void onNew(Object mother);
        public void onDelete(Object mother);
        public void preUpdate(Object mother);
        public void postUpdate(Object mother);

    }


}
