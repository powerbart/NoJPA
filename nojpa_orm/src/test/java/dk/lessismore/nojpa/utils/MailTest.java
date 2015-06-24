package dk.lessismore.nojpa.utils;

import org.junit.Test;

/**
 * Created by niakoi on 24/6/15.
 */
public class MailTest {
    @Test
    public void testMail() throws Exception {
        Mailer.send(true, "my subject", "some body", "support@qxl.com", new String[] {"atanas.balevsky@gmail.com"}, null, null);

    }
}
