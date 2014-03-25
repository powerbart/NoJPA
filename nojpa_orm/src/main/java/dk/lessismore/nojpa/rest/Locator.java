package dk.lessismore.nojpa.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: niakoi
 * Date: 3/24/14
 * Time: 11:41 PM
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Locator {
    Class<? extends ObjectLocator> locator();
}