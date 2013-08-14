package dk.lessismore.reusable_v4.net.others;

import com.sun.mail.iap.ByteArray;
import dk.lessismore.reusable_v4.reflection.util.ReflectionUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: seb
 * Date: 08-04-11
 * Time: 13:18
 * To change this template use File | Settings | File Templates.
 */
public class HttpClientTest {




//    @Test
//    public void testSimpleCall() throws Exception {
//        String s = HttpClient.sendHttpGetRequest("http://www.dr.dk/");
//        System.out.println("s = " + s);
//    }
//TODO
//    @Test
//    public void testSimpleCall() throws Exception {
//
//        for(int i = 0; i < 20; i++){
//            HttpClient httpclient = new DefaultHttpClient();
//            HttpGet httpget = new HttpGet("http://d2.less-is-more.dk/");
//            HttpResponse response = httpclient.execute(httpget);
//            HttpEntity entity = response.getEntity();
//            ByteArrayOutputStream ba = new ByteArrayOutputStream((int) entity.getContentLength());
//            entity.writeTo(ba);
//            String s = new String(ba.toByteArray(), 0, ba.size());
//            if(s.length() > 20){
//                s = s.substring(0,20).replaceAll("\n", "").replaceAll("\r", "").replaceAll("\t", "");
//            }
//            System.out.println("============================================================");
//            System.out.println("s("+ i +") = " + s);
//            System.out.println("============================================================");
//            Thread.sleep(1 * 100);
//        }
//    }




    @Test
    public void testDoubleDns() throws Exception {
//        for(int i = 0; i < 200; i++){
//            try{
//                String s = HttpClient.sendHttpGetRequest("http://d2.less-is-more.dk/");
//                if(s.length() > 20){
//                    s = s.substring(0,20).replaceAll("\n\r\t ", "");
//                }
//                System.out.println("============================================================");
//                System.out.println("s("+ i +") = " + s);
//                System.out.println("============================================================");
//                Thread.sleep(5 * 1000);
//            } catch (Exception e){
//                e.printStackTrace();
//            }
//
//        }
    }




}
