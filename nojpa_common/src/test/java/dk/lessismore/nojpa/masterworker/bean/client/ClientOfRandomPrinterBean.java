package dk.lessismore.nojpa.masterworker.bean.client;

import dk.lessismore.nojpa.masterworker.bean.RemoteBeanService;
import dk.lessismore.nojpa.masterworker.bean.RandomPrinterBean;
import dk.lessismore.nojpa.masterworker.bean.worker.BeanExecutor;

/**
 * Created : by IntelliJ IDEA.
 * User: seb
 * Date: 21-10-2010
 * Time: 14:56:38
 * To change this template use File | Settings | File Templates.
 */
public class ClientOfRandomPrinterBean {


    public static void main(String[] args) throws InterruptedException {

        RandomPrinterBean randomPrinterBean = RemoteBeanService.newRemoteBean(RandomPrinterBean.class, BeanExecutor.class);
        Thread.sleep(5000);
        randomPrinterBean.setStringToPrint("MyStringToPrint");
        randomPrinterBean.setTimes(3, 1000);
        randomPrinterBean.print();
        Thread.sleep(2500);


        System.out.println(" ---------- NOW CALLING RETURNS ---------------- 1");
        System.out.println("randomPrinterBean.getMyName() = " + randomPrinterBean.getMyName());
        System.out.println(" ---------- NOW CALLING RETURNS ---------------- 2");
        System.out.println("randomPrinterBean.getMyCalendar() = " + randomPrinterBean.getMyCalendar());
        System.out.println(" ---------- NOW CALLING RETURNS ---------------- 3");
        System.out.println("randomPrinterBean.getMyInt() = " + randomPrinterBean.getMyInt());
        System.out.println(" ---------- NOW CALLING RETURNS ---------------- 4");
        System.out.println("randomPrinterBean.getMyInteger() = " + randomPrinterBean.getMyInteger());


        randomPrinterBean.closeDownRemoteBean();
        
        Thread.sleep(5000);

    }





}
