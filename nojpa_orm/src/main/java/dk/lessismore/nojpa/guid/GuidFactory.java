package dk.lessismore.nojpa.guid;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * This class can make an Global Unique Identifier (GUID). Translated - the class can
 * produce an unique id string that can be used as primary key, in forinstance an
 * database tupel. The class allso contains its own singelton/factory.
 *
 * @author Raymond
 * @version 1.0
 */
public class GuidFactory {

    static public GuidFactory factory = null;
    Random random = new Random();

    static public GuidFactory getInstance() {
        if (factory == null)
            factory = new GuidFactory();
        return factory;
    }

    public String returnConvertedGuid() {
        return makeGuid();
    }

    static final private char[] digits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    static final private int shift = 4;
    static final private long mask = (1 << 4) - 1;

    static final public int guidLenght = 32;

    public String makeGuid() {
        char[] buf = new char[guidLenght];
        int charPos = guidLenght - 1;

        long rnd = random.nextLong();
        while (charPos >= 16) {
            buf[charPos--] = digits[(int) (rnd & mask)];
            rnd >>>= shift;
        }
        {
            long time = System.nanoTime();
            while (charPos >= 4) {
                buf[charPos--] = digits[(int) (time & mask)];
                time >>>= shift;
            }
        }

        {
            String time = Long.toHexString(System.currentTimeMillis()).toUpperCase();
            for(int i = 0; i < 4; i++) {
                buf[i] = time.charAt(i);
            }
        }
        return new String(buf);
    }

    public static void main(String[] args) {
        System.out.println("--" + Long.toHexString(System.currentTimeMillis()).toUpperCase());
        Long y5 = 157680000000L;
        for(int i = 1; i < 100; i++) {
            System.out.println(Long.toHexString((i * y5 ) + System.currentTimeMillis()).toUpperCase());

        }

    }
}
