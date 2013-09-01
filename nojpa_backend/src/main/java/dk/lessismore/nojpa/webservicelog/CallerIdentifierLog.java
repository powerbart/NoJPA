package dk.lessismore.nojpa.webservicelog;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.annotations.DbStrip;

public interface CallerIdentifierLog extends ModelObjectInterface {

    public String getLoginUserName();
    @DbStrip(stripItSoft = true)
    public void setLoginUserName(String loginUserName);

    public String getSystemUserName();
    @DbStrip (stripItSoft = true)
    public void setSystemUserName(String systemUserName);

    public String getCallerApplicationName();
    @DbStrip (stripItSoft = true)
    public void setCallerApplicationName(String callerApplicationName);

    public String getCallerMethodName();
    @DbStrip (stripItSoft = true)
    public void setCallerMethodName(String callerMethodName);

    public String getCallerHostName();
    @DbStrip (stripItSoft = true)
    public void setCallerHostName(String callerHostName);

    public String getCallerVersion();
    @DbStrip (stripItSoft = true)
    public void setCallerVersion(String callerVersion);

}
