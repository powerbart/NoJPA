package dk.lessismore.nojpa.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by niakoi on 24/6/15.
 */
public class MailTest {

    @Test
    public void testMail_prod() throws Exception {
        Mailer.send(true, "my subject ", "some body ", "QXL.com <support@qxl.com>", new String[]{"atanas.balevsky@gmail.com"}, null);


    }
//
//    @Test
//    public void testMail() throws Exception {
//        ExecutorService executorPool = Executors.newFixedThreadPool(50);
//        ArrayList<Callable<Void>> mails = new ArrayList<Callable<Void>>();
//        for (int i = 1; i <= 100; i++) {
//            mails.add(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    Mailer.send(true, "my subject ", "some body ", "less-is-more@lauritz.dev", new String[]{"atanas.balevsky@gmail.com"}, null);
//                    return null;
//                }
//            });
//
//        }
//        executorPool.invokeAll(mails);
//
//    }
//
//    @Test
//    public void testMail2() throws Exception {
//        ExecutorService executorPool = Executors.newFixedThreadPool(50);
//        ArrayList<Callable<Void>> mails = new ArrayList<Callable<Void>>();
//        for (int i = 1; i <= 100; i++) {
//            mails.add(new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    Mailer.send(true, "my subject ", "some body ", "less-is-more@lauritz.dev", new String[]{"atanas.balevsky@gmail.com"}, null);
//                    return null;
//                }
//            });
//
//        }
//        executorPool.invokeAll(mails);
//
//    }
}
