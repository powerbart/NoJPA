package dk.lessismore.reusable_v4.properties;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Specifies the default textual value for a field, which will be used if the field is not present in the property file.
 * All values must be specified as strings, as they would appear in the property file (because annotations cannot
 * depend on the type of method they are annotating). Example:
<pre>
    &#064;Default("42")
    public int getAge()
</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Default {
    String value();
}
