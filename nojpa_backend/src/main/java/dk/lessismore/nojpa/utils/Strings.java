package dk.lessismore.nojpa.utils;

import java.util.Arrays;
import java.util.Iterator;

public class Strings {

    public static String separateBy(Object[] objects, String separator) {
        return separateBy(Arrays.asList(objects), separator);
    }

    public static String separateBy(Iterable objects, String separator) {
        StringBuilder builder = new StringBuilder();
        Iterator iterator = objects.iterator();
        while (iterator.hasNext()) {
            Object o = iterator.next();
            builder.append(o);
            if (iterator.hasNext()) builder.append(separator);
        }
        return builder.toString();
    }
}
