package dk.lessismore.nojpa.reflection.db.annotations;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * An annotation that states whether or not to strip "dangerous" characters and prettify a string field.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DbStrip {
    /**
     * stripItHard = false + stripItSoft = false : then no strip at all
     * stripItHard = false + stripItSoft = true : only replace ' and " to -> `
     * stripItHard = true || no DbStrip over the set method : replaceAll('|\"", "`").replaceAll("/|&|'|<|>|;|\\\\", "")) + first char to upper case 
     * @return
     */
    public boolean urlEncode() default false;
    public boolean stripItHard() default true;
    public boolean stripItSoft() default false;
}
