package dk.lessismore.nojpa.reflection.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Calendar;

/**
 * Created : with IntelliJ IDEA.
 * User: seb
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchIndex {

    public TimeFrame timeFrame() default TimeFrame.MONTH;
    public enum TimeFrame { DAY, MONTH }


}

