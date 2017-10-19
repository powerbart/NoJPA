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

        RandomPrinterBean randomPrinterBean = RemoteBeanService.newRemoteBean(RandomPrinterBean.class);
//        Thread.sleep(5000);
//        randomPrinterBean.setStringToPrint("MyStringToPrint");
//        randomPrinterBean.setTimes(3, 1000);
//        randomPrinterBean.print();
//        Thread.sleep(2500);
//
//
        System.out.println(" ---------- NOW CALLING RETURNS ---------------- 1");
//        System.out.println("randomPrinterBean.getMyName() = " + randomPrinterBean.getMyName());
//        System.out.println(" ---------- NOW CALLING RETURNS ---------------- 2");
//        System.out.println("randomPrinterBean.getMyCalendar() = " + randomPrinterBean.getMyCalendar());
//        System.out.println(" ---------- NOW CALLING RETURNS ---------------- 3");
//        long start = System.currentTimeMillis();
//        System.out.println("randomPrinterBean.getMyInt() = " + randomPrinterBean.getMyInt());
//        System.out.println("TIME-OF-ONE-CALL: " + (System.currentTimeMillis() - start));
//        System.out.println(" ---------- NOW CALLING RETURNS ---------------- 4");
//        System.out.println("randomPrinterBean.getMyInteger() = " + randomPrinterBean.getMyInteger());
//        System.out.println(" ---------- START MAKING 100 calls ---------------- 5");
//        long start100 = System.currentTimeMillis();
//        for(int i = 0; i < 100; i++){
//            System.out.println(i + " = " + randomPrinterBean.getMyInt());
//        }
//        long end100 = System.currentTimeMillis();
//        System.out.println("======= 100 calls in TIME("+ (end100 - start100) +")");

        System.out.println("x1 = " + randomPrinterBean.getMyInt());
        Thread.sleep(1000 * 10);
        System.out.println("x2 = " + randomPrinterBean.getMyInt());
        System.out.println("x3 = " + randomPrinterBean.getMyInt());
        Thread.sleep(1000 * 10);
        System.out.println("x4.1 = " + randomPrinterBean.getMyInt());
        System.out.println("x4.2 = " + randomPrinterBean.getMyInt());
        System.out.println("x4.3 = " + randomPrinterBean.getMyInt());
        System.out.println("x4.4 = " + randomPrinterBean.getMyInt());
        System.out.println("x4.5 = " + randomPrinterBean.getMyInt());
        Thread.sleep(1000 * 60);
        randomPrinterBean.closeDownRemoteBean();
        
        Thread.sleep(5000);

    }





}
