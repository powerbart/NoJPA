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

    public static String replace(String find, String replace, String bigString) {
        int indexStart = 0;
        int lengthOfFind = find.length();
        while (indexStart != -1) {
            indexStart = bigString.indexOf(find, indexStart);
            if (indexStart != -1) {
                bigString = bigString.substring(0, indexStart)
                        + replace + bigString.substring(indexStart + lengthOfFind);
                indexStart++;
            }
        }
        return bigString;
    }


}
