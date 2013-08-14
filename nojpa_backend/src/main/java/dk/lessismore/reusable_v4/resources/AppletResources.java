package dk.lessismore.reusable_v4.resources;

import java.applet.*;
import java.util.*;
import dk.lessismore.reusable_v4.resources.*;

/**
 * This class represents the parameters from a applet as resources.
 * You should notice that you can not get a list of the parameter names
 * unless you define them in the applet.
 *
 * @author LESS-IS-MORE ApS
 * @version 1.0 25-7-02
 */

public class AppletResources extends Resources {

    /**
     * The applet object.
     */
    private Applet _applet = null;

    public AppletResources(Applet applet) {
        _applet = applet;
    }
    public String getResource(String key) {
        if(_applet != null)
            return _applet.getParameter(key);
        else
            return null;
    }
    public boolean gotResources() {
        return _applet != null;
    }
    public Enumeration getResourceNames() {

        if(_applet != null) {
            String[][] parameterInfo = _applet.getParameterInfo();
            if(parameterInfo != null) {
                Vector parameterNames = new Vector(parameterInfo.length);
                for(int i = 0; i < parameterInfo.length; i++) {
                    parameterNames.addElement(parameterInfo[i][0]);
                }
                return parameterNames.elements();
            }
        }
        return null;
    }
    public boolean canGetParameterNameList() {
        return false;
    }

    public boolean setString(String key, String value) {
        return true;
    }

}
