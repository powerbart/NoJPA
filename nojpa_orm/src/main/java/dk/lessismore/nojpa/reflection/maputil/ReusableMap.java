package dk.lessismore.nojpa.reflection.maputil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 19-08-2010
 * Time: 15:39:19
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReusableMap {

    public String[] mapNames();
    
}
