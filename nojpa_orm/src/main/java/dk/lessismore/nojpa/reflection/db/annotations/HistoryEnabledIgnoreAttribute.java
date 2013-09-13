package dk.lessismore.nojpa.reflection.db.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Created : by IntelliJ IDEA.
 * User: bart
 * Date: 28-01-2009
 * Time: 12:45:42
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HistoryEnabledIgnoreAttribute {
}
