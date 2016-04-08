package dk.lessismore.nojpa.masterworker.bean.worker;

import dk.lessismore.nojpa.masterworker.bean.RandomPrinterBean;

import java.util.Calendar;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 */
public class RandomPrinterBeanImpl implements RandomPrinterBean {

    String str = "string is not set ... ";
    int timesOfPrinting = -1;
    int sleepInMillis = -1;

    public void setStringToPrint(String str) {
        this.str = str;
    }

    public void setTimes(int timesOfPrinting, int sleepInMillis) {
        this.timesOfPrinting = timesOfPrinting;
        this.sleepInMillis = sleepInMillis;
    }

    public void print() throws InterruptedException {
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < timesOfPrinting; j++){
                System.out.println("("+ j +"/"+ timesOfPrinting +")("+ i  +"/"+ 10 +") " + str);
            }
            Thread.sleep(sleepInMillis);
        }
    }

    public String getMyName() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "MySmallName";
    }

    public int getMyInt() {
        return 666;
    }

    public Integer getMyInteger() {
        return 777;
    }

    public Calendar getMyCalendar() {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.HOUR_OF_DAY, -3);
        return instance;
    }

    public void closeDownRemoteBean() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public double getProgress() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }


}
