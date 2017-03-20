package dk.lessismore.nojpa.properties;

import dk.lessismore.nojpa.resources.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Design decisions:
// - It is designed to fit most existing property files.
// - It uses the standard camelCase Java naming convention for fields (ie. don't write abbreviations in upper case).
// - It supports primitive types because their format is defined by Java.
// - String is handled because it is the default type of property value, and Java has special support for strings.
// - Arrays are supported because they are convenient and special in Java, retaining the types of it's elements at
//   runtime. None of the collection types do this because of type erasure for generics.
// - It aims to be a lightweight schema for property files. It supports the Required annotation for ensuring that null
//   does not need to be handled anywhere else by the application programmer. It supports the Default annotation as
//   a simple way to give a default value to a field, so this does not need to be handled anywhere else either.
// - This means that you only need to look at the interface to see the basic invariants.
// It currently does not support setters and save().

/**
 * An proxy object that reads fields from a property files and converts their value to the required type.
 * Strings are trimmed unless they are quoted. This is what you want "most of the time".
 * They also cannot contain commas and double quotes when not quoted.
 * Array elements are comma separated.
 * @see #getInstance(Class)
 */
public class PropertiesProxy implements InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(PropertiesProxy.class);
    private static final long CHECK_INTERVAL = 60000L;
    private static final String POSTFIX = "Properties";
    private static final Pattern ARRAY_PATTERN = Pattern.compile(
            "\\s*([\"]([^\"]|[\"][\"])*[\"]|(\\s*[^\\s,]+)*)\\s*([,]?)", Pattern.MULTILINE);
    private final Class<?> interfaceClass;
    private final Set<PropertiesListener> listeners = new HashSet<PropertiesListener>();
    private java.util.Properties properties;
    private Thread thread = null;

    /**
     * Creates a Properties object based on the supplied interface.
     * <p>
     * For example, ExWhyZeeProperties with the getter {@code Integer getFooBar()} will load the optional
     * field fooBar from the properties file ExWhyZee.properties (and if it doesn't exist, return null).
     * If (and only if) a listener is added, the file will be checked regularly for changes.
     * <p>
     * The interface must only have getters and those must have no arguments.
     * It supports all primitive types, which are automatically parsed from the string in the property file.
     * If 'int' is used rather than 'Integer', or @Required is used for a getter, it is an error to leave the field
     * out of the property file. In that case the loader will report it and an exception will be thrown on access.
     * It also supports fields of type String. Additionally, it supports arrays:
<pre>
    myList = 1, 2, 3
    => new int[] {1, 2, 3}

    myArray = John Foo, "Peter ""The Thug"" Bar"
    => new String[] {"John Foo", "Peter \"The Thug\" Bar"}

    myCollection =
    => new String[] {}

    # myMissing is not specified
    => null
</pre>
     * Please note that non-quoted strings are automatically trimmed
     * and cannot contain commas (,), quotes (") or line breaks, but internal spaces and tabs will be preserved.
     * A special rule is: if right hand side is pure whitespace or empty, the list is empty.
     * If you need exactly one empty string, you must quote it (or it will be parsed as the empty array).
     */
    public static <T extends Properties> T getInstance(Class<T> propertyFileClass) {
        return (T) Proxy.newProxyInstance(
	        propertyFileClass.getClassLoader(),
	        new Class<?>[] { propertyFileClass },
            new PropertiesProxy(propertyFileClass));
    }

    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        String name = method.getName();
        if(name.equals("addListener")) {
            if(objects.length != 1) {
                throw new PropertiesException("Wrong number of arguments in addListener in " + 
                        interfaceClass.getName());
            }
            addListener((PropertiesListener) objects[0]);
            return null;
        } else if(name.equals("removeListener")) {
            if(objects.length != 1) {
                throw new PropertiesException("Wrong number of arguments in removeListener in " +
                        interfaceClass.getName());
            }
            removeListener((PropertiesListener) objects[0]);
            return null;
        } if(name.startsWith("get")) {
            if(objects != null && objects.length != 0) {
                throw new PropertiesException("Getters cannot have arguments in " + interfaceClass.getName());
            }
            String field = getMethodKey(method);
            String place = interfaceClass.getName() + " field " + field;
            String text = properties.getProperty(field);
            if(text == null) text = getDefault(method);
            if(text == null && isRequired(method)) {
                throw new PropertiesException("The field '" + field + "' is required in " + place);
            }
            Class<?> type = method.getReturnType();
            if(text == null) {
                return text;
            }
            return parse(text, type, place);
        } else {
            throw new PropertiesException("This method is not supported: " +
                    method.getName() + " in " + interfaceClass.getName());
        }
    }

    private PropertiesProxy(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
        reloadProperties(true);
    }

    public static Object parse(String text, Class<?> type, String place) {
        text = text.trim();
        if(type.isArray()) {
            Class<?> kind = type.getComponentType();
            if(kind == null) kind = Object.class;
            Object array;
            if(text.isEmpty()) return Array.newInstance(kind, 0);
            String[] elements = parseElements(text, place);
            array = Array.newInstance(kind, elements.length);
            for(int i = 0; i < elements.length; i++) {
                Array.set(array, i, parse(elements[i], kind, place));
            }
            return array;
        }
        if(text.startsWith("\"") && text.endsWith("\"")) {
            text = text.substring(1, text.length() - 1).replace("\"\"", "\"");
        } else if(text.contains("\"")) {
            throw new PropertiesException("Syntax error in non-quoted value (embedded quote) in " + place);
        } else if(text.contains(",")) {
            throw new PropertiesException("Syntax error in non-quoted value (embedded comma) in " + place);
        } else if(text.contains("\r") || text.contains("\n")) {
            throw new PropertiesException("Syntax error in non-quoted value (embedded line break) in " + place);
        }
        if(type == String.class) {
            return text;
        } else if(type == Calendar.class){
            if(text.equals("now")) {
                return Calendar.getInstance();
            } else {
                Long parsedLong = null;
                try {
                    parsedLong = Long.parseLong(text);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(parsedLong);
                    return calendar;
                } catch (Exception e) {
                    // didn/t manage to parse a long
                }

                return null;
            }
        } else if(instance(type, Integer.class, Integer.TYPE)) {
            try {
                return Integer.parseInt(text);
            } catch(NumberFormatException e) {
                throw new PropertiesException("Not an integer in " + place, e);
            }
        } else if(instance(type, Long.class, Long.TYPE)) {
                try {
                    return Long.parseLong(text);
                } catch(NumberFormatException e) {
                    throw new PropertiesException("Not a long in " + place, e);
                }
        } else if(instance(type, Float.class, Float.TYPE)) {
            try {
                return Float.parseFloat(text);
            } catch(NumberFormatException e) {
                throw new PropertiesException("Not a float in " + place, e);
            }
        } else if(instance(type, Double.class, Double.TYPE)) {
            try {
                return Double.parseDouble(text);
            } catch(NumberFormatException e) {
                throw new PropertiesException("Not a double in " + place, e);
            }
        } else if(instance(type, Short.class, Short.TYPE)) {
            try {
                return Short.parseShort(text);
            } catch(NumberFormatException e) {
                throw new PropertiesException("Not a short in " + place, e);
            }
        } else if(instance(type, Byte.class, Byte.TYPE)) {
            try {
                return Byte.parseByte(text);
            } catch(NumberFormatException e) {
                throw new PropertiesException("Not a byte in " + place, e);
            }
        } else if(instance(type, Character.class, Character.TYPE)) {
            if(text.length() != 1) {
                throw new PropertiesException("Not a character (must have length 1) in " + place);
            }
            return text.charAt(0);
        } else if(instance(type, Boolean.class, Boolean.TYPE)) {
            if(text.equalsIgnoreCase("true")) return true;
            else if(text.equalsIgnoreCase("false")) return false;
            else throw new PropertiesException("Not a boolean - must be 'true' or 'false' in " + place);
        } else {
            throw new PropertiesException("This type is not supported: " + type + " in " +  place);
        }
    }

    private static String[] parseElements(String text, String place) {
        List<String> elements = new ArrayList<String>();
        Matcher matcher = ARRAY_PATTERN.matcher(text);
        do {
            if(matcher.find()) {
                elements.add(matcher.group(1));
            } else {
                throw new PropertiesException("Syntax error in list definition (premature end) in " + place);
            }
        } while(matcher.group(4).equals(","));
        if(matcher.end() < text.length()) {
            throw new PropertiesException(
                    "Syntax error in list definition (unexpected character in '" + text + "') in " + place);
        }
        return elements.toArray(new String[elements.size()]);
    }

    private void startThread() {
        if(thread != null) return;
        thread = new Thread(new Runnable() {
            public void run() {
                try {
                    while(true) {
                        Thread.sleep(CHECK_INTERVAL);
                        try {
                            reloadProperties(false);
                        } catch (PropertiesException e) {
                            log.error("Exception in updater, continuing: " + interfaceClass.getName(), e);
                        }
                    }
                } catch (InterruptedException e) {
                    // We're done, so just return
                } catch (RuntimeException e) {
                    log.error("Exception in updater, stopping: " + interfaceClass.getName(), e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private static boolean instance(Class<?> a, Class<?> b, Class<?> c) {
        return a == b || a == c;
    }

    private boolean isRequired(Method method) {
        boolean required = method.getAnnotation(Required.class) != null;
        boolean optional = method.getAnnotation(Default.class) != null;
        if(required && optional) {
            log.error("A field cannot be Required and have a Default value at the same time in method " +
                    method.getName() + " in class " + interfaceClass.getName());
        }
        return (required || method.getReturnType().isPrimitive()) && !optional;
    }

    private String getDefault(Method method) {
        Default annotation = method.getAnnotation(Default.class);
        return annotation != null ? annotation.value() : null;
    }

    private synchronized void addListener(PropertiesListener listener) {
        listeners.add(listener);
        //Don't want to reload the properties all the time.... startThread();
    }

    private synchronized void removeListener(PropertiesListener listener) {
        listeners.remove(listener);
    }

    private synchronized void reloadProperties(boolean fail) {
        String className = interfaceClass.getSimpleName();
        if(className.endsWith(POSTFIX)) className = className.substring(0, className.length() - POSTFIX.length());
        String fileName = "/" + className + ".properties";
        java.util.Properties newProperties;
        try {
            newProperties = new java.util.Properties();
            System.out.println("Loading properties: fileName = " + fileName);
            URL url = Resources.class.getResource(fileName);
            System.out.println("Loading properties: url = " + url);
            InputStream stream = url.openStream();
            newProperties.load(stream);
            stream.close();
        } catch(IOException e) {
            if(fail) throw new PropertiesException(e);
            else newProperties = null;
        }
        if(fail || newProperties != null) {
            boolean notify = properties != null && !properties.entrySet().equals(newProperties.entrySet()); 
            properties = newProperties;
            if(notify) {
                for(PropertiesListener listener: listeners) {
                    listener.onChanged();
                }
            }
        }
        check();
    }

    private void check() {
        Set<String> acceptedKeys = getMethodKeys();
        for(Object key: properties.keySet()) {
            String field = (String) key;
            if(!acceptedKeys.contains(field)) {
                log.warn("Unknown field '" + field + "' in properties file for " + interfaceClass.getName());
            }
        }
        for(Method method: getMethods()) {
            String field = getMethodKey(method);
            if(isRequired(method) && !properties.containsKey(field)) {
                log.error("Missing required field '" + field + "' in properties file for " + interfaceClass.getName());
            }
        }
    }

    private Set<Method> getMethods() {
        Set<Method> methods = new HashSet<Method>();
        for(Method method: interfaceClass.getMethods()) {
            if(method.getName().startsWith("get")) methods.add(method);
        }
        return methods;
    }

    private Set<String> getMethodKeys() {
        Set<String> keys = new HashSet<String>();
        for(Method method: getMethods()) {
            keys.add(getMethodKey(method));
        }
        return keys;
    }

    private String getMethodKey(Method method) {
        String name = method.getName();
        if(!name.startsWith("get")) throw new PropertiesException("Not a getter: " + method);
        return name.substring(3, 4).toLowerCase() + name.substring(4);
    }
}
