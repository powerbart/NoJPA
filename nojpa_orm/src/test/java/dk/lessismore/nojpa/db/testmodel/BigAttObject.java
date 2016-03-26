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
    public void setMyBoolean1(boolean a);


    @Default("false")
    public boolean getMyBoolean2();
    public void setMyBoolean2(boolean a);

    public boolean getMyBoolean3();
    public void setMyBoolean3(boolean a);

    public int getMyInt0();
    public void setMyInt0(int a);

    @Default("666")
    public int getMyInt666();
    public void setMyInt666(int a);


    public long getMyLong0();
    public void setMyLong0(long a);

    @Default("666")
    public long getMyLong666();
    public void setMyLong666(long a);



    public float getMyFloat0();
    public void setMyFloat0(float a);

    @Default("1")
    public float getMyFloat1();
    public void setMyFloat1(float a);


    public double getMyDouble();
    public void setMyDouble(double a);


    public String getStringNull();
    public void setStringNull(String a);

    @Default("Seb")
    public String getStringSeb();
    public void setStringSeb(String a);



    public Calendar getCalendarNull();
    public void setCalendarNull(Calendar a);


    @Default("now")
    public Calendar getCalendarNow();
    public void setCalendarNow(Calendar a);

    

}
