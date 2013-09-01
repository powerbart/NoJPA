package dk.lessismore.nojpa.properties;

// Justification:
// Loading and parsing of properties files are often done in the class in which the properties are needed.
// This means that checking for existence and parsing of values is coded from scratch each time it is needed.
// It also means that the class has to be concerned with parsing and the like, which limits separation of concerns.
// Often if the same property file is needed in multiple classes, the code for loading the properties is repeated in
// each class, typically for some subset of the fields. This means that code is duplicated and may go out of sync,
// resulting in multiple contradictionary constraints placed on the field. It also means you have to read all of the
// classes that uses the property file to discover which fields the file should contain and how they should look. These
// classes may even be hard to find, since they are only related by a string occuring somewhere in each class.
// It also means that property files look different, with different naming conventions and value formats, different
// rules for when strings are trimmed and what constitutes a list.
// Sometimes it is convenient to alter the properties file while the program is running, and have the react to it. This
// typically requires a thread and a listener framework, which really shouldn't be recreated from scratch each time.
// The classes in this package are designed to eliminate all of the above weaknesses.

/**
 * The base interface that property file interfaces should extend with getters.
 * Interfaces that extend this one should end with the suffix "Properties".
 * The corresponding properties file will be named like the extending interface,
 * except the "Properties" suffix is replaced by ".properties".
<pre>
    public interface ServerProperties extends Properties {
        @Required
        public String getHost();
        public int getPort();
        @Default("/")
        public String getPath();
    }
</pre>
 * With a corresponding property file Server.properties:
<pre>
    host = localhost
    port = 8080
    # path can be omitted
</pre>
 * @see PropertiesProxy
 */
public interface Properties {
    /** Adds a listener that will be notified each time the property file has changed. */
    public void addListener(PropertiesListener listener);
    public void removeListener(PropertiesListener listener);
}
