package dk.lessismore.nojpa.webservicelog;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.reflection.db.annotations.DbStrip;

import java.util.Calendar;

public interface CallLog extends ModelObjectInterface {
    public CallLog getParent();
    public void setParent(CallLog value);

    public String getMethodName();
    @DbStrip (stripItSoft = true)
    public void setMethodName(String name);

    public String getClassName();
    @DbStrip (stripItSoft = true)
    public void setClassName(String name);

    public int getMaxLogLevel();
    public void setMaxLogLevel(int maxLogLevel);

    public long getMilliTime();
    public void setMilliTime(long maxLogLevel);

    public double getFailed();
    public void setFailed(double failed);

    Calendar getLastModified();
    void setLastModified(Calendar lastModified);
}
