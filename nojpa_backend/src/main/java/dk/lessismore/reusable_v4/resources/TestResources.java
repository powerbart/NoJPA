package dk.lessismore.reusable_v4.resources;

import java.util.*;
import java.net.*;
import java.io.*;
import javax.servlet.http.*;
/**
 * This class represents the attributes in a hashtable. The class designed
 * to make test cases where the hashtable is filled with a number of attributes.e
 * In this way you can simulate a http request or a property file etc.
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class TestResources  {

    /**
     * The hashtable. key=Attribute/parameter name; value=The string of the attribute.
     */
    private Map testResourcesMap = new HashMap();

    /**
     * Flag indicating if we can get a list of attribute names in the hashtable.
     * This is for test purpose only; and should be set to <tt>true</tt>
     */
    private boolean canGetParameterNameList = true;

}
