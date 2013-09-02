package dk.lessismore.nojpa.masterworker.bean;

import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: seb
 * Date: 21-10-2010
 * Time: 14:47:13
 * To change this template use File | Settings | File Templates.
 */
public interface RandomPrinterBean extends RemoteBeanInterface {


    public void setStringToPrint(String str);

    public void setTimes(int timesOfPrinting, int sleepInMillis);

    public void print() throws InterruptedException;


    public String getMyName();

    public int getMyInt();

    public Integer getMyInteger();

    public Calendar getMyCalendar();


}
