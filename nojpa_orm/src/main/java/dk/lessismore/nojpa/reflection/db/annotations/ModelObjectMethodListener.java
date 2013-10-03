package dk.lessismore.nojpa.reflection.db.annotations;

import javax.persistence.Transient;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 11-07-11
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelObjectMethodListener {

    public Class<? extends MethodListener> methodListener();

    public static interface MethodListener {

        public void preRun(Object mother, String methodName, Object[] args);
        public Object postRun(Object mother, String methodName, Object preResult, Object[] args);

    }

}