package dk.lessismore.nojpa.db.testmodel;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.properties.Default;

import java.util.Calendar;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 02-03-2011
 * Time: 10:45:37
 * To change this template use File | Settings | File Templates.
 */
public interface BigAttObject extends ModelObjectInterface {


    @Default("true")
    public boolean getMyBoolean1();
    public void setMyBoolean1(boolean _);


    @Default("false")
    public boolean getMyBoolean2();
    public void setMyBoolean2(boolean _);

    public boolean getMyBoolean3();
    public void setMyBoolean3(boolean _);

    public int getMyInt0();
    public void setMyInt0(int _);

    @Default("666")
    public int getMyInt666();
    public void setMyInt666(int _);


    public long getMyLong0();
    public void setMyLong0(long _);

    @Default("666")
    public long getMyLong666();
    public void setMyLong666(long _);



    public float getMyFloat0();
    public void setMyFloat0(float _);

    @Default("1")
    public float getMyFloat1();
    public void setMyFloat1(float _);


    public double getMyDouble();
    public void setMyDouble(double _);


    public String getStringNull();
    public void setStringNull(String _);

    @Default("Seb")
    public String getStringSeb();
    public void setStringSeb(String _);



    public Calendar getCalendarNull();
    public void setCalendarNull(Calendar _);


    @Default("now")
    public Calendar getCalendarNow();
    public void setCalendarNow(Calendar _);

    

}
