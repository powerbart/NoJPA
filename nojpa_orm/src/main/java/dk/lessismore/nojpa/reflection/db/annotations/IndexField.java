package dk.lessismore.nojpa.reflection.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created : by IntelliJ IDEA.
 * User: niakoi
 * Date: Aug 20, 2010
 * Time: 6:18:12 PM
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IndexField {

    String value() default "";

    DatabaseIndexClass clz() default DatabaseIndexClass.NORMAL;

    public enum DatabaseIndexClass {
        NORMAL, UNIQUE, FULLTEXT, SPATIAL
    }

}
