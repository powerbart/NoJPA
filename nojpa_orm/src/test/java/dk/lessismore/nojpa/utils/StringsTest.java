package dk.lessismore.nojpa.utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.Collections;

public class StringsTest {

    @Test
    public void separateByTest() {
        assertEquals("1, 2, 3, 4", Strings.separateBy(Arrays.asList(1,2,3,4), ", "));
        assertEquals("1, 2, 3, 4", Strings.separateBy(new Integer[] {1,2,3,4}, ", "));
        assertEquals("1", Strings.separateBy(Arrays.asList(1), ", "));
        assertEquals("", Strings.separateBy(Collections.emptyList(), ", "));
    }

    public static void main(String[] args) {
        System.out.println("Hej med dig\\asda".replaceAll("/|&|'|<|>|;|\\\\", ""));


    }

}
