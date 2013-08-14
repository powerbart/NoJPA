package dk.lessismore.reusable_v4.resources;

import java.util.*;
import java.net.*;
import java.io.*;
import javax.servlet.http.*;
/**
 * This class represents the resources from a http request. Use this to
 * make a wrapper class when deling with the attributes from a http request.
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */
public class RequestResources extends Resources {

    /**
     * The http request object.
     */
    private HttpServletRequest request = null;

    public RequestResources(HttpServletRequest request) {
        setRequest(request);
    }
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    public String getResource(String key) {
        if(request != null)
            return request.getParameter(key);
        else
            return null;
    }
    public boolean gotResources() {
        return request != null;
    }
    public Enumeration getResourceNames() {

        return request.getParameterNames();
    }
    public boolean canGetParameterNameList() {
        return true;
    }
    public boolean setString(String key, String value) {
        return true;
    }
}
