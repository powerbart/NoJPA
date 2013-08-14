package dk.lessismore.reusable_v4.properties;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Specifies that a field must appear in the property file. It is cannot be used with the Default annotation.
 * Fields with primitive types (those that cannot be null, such as int, boolean and char)
 * are implicitly required if they don't have a Default annotation. Example:
<pre>
    &#064;Required
    public String getHost()
</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Required {
}
