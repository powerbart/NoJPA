package dk.lessismore.nojpa.reflection.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created : with IntelliJ IDEA.
 * User: seb
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchField {

    public static final String NULL = "";

    public boolean translate() default false;
    public boolean searchReverse() default false;
    public float boostFactor() default 3f;
    public float reverseBoostFactor() default 0.3f;
    public String dynamicSolrPostName() default NULL;




}

