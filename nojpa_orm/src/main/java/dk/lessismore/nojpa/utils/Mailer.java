package dk.lessismore.nojpa.utils;

import dk.lessismore.nojpa.resources.PropertyResources;
import dk.lessismore.nojpa.resources.PropertyService;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Created : with IntelliJ IDEA.
 * User: seb
 */

// TODO: Find a mail server that works
public class Mailer {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Mailer.class);
    private static String smtpHost;
    private static String smtpUser;
    private static String smtpPass;
    private static int smtpPort = 0;
    private static final PropertyResources propertyResources;

    static {
        propertyResources = PropertyService.getInstance().getPropertyResources(Mailer.class);
        smtpHost = propertyResources.getString("smtpHost");
        smtpUser = propertyResources.getString("smtpUser");
        smtpPass = propertyResources.getString("smtpPass");
        try {
            smtpPort = propertyResources.getInt("smtpPort");
        } catch (Exception e) {
            // just ignore it
        }
        if (smtpHost == null) log.error("No property smtpHost was found");
    }
//
//    private static class Authenticator extends javax.mail.Authenticator {
//        private PasswordAuthentication authentication;
//
//        public Authenticator() {
//            String username = smtpUser;
//            String password = smtpPass;
//            authentication = new PasswordAuthentication(username, password);
//        }
//
//        protected PasswordAuthentication getPasswordAuthentication() {
//            return authentication;
//        }
//    }


    static void debug(String e) {
        log.debug(e);
        System.out.println((new Date()) + ":" + e);
    }


    public static void send(boolean html, String subject, String body, String from, String[] recipents, String[] bccRecipients, File... attachments) {
        Properties prop = System.getProperties();

        System.out.println("prop = " + prop);
        System.out.println("smtpUser = " + smtpUser);
        System.out.println("smtpPass = " + smtpPass);
        System.out.println("smtpHost = " + smtpHost);

        if (smtpUser != null) {
            prop.setProperty("mail.smtp.submitter", smtpUser);
            prop.setProperty("mail.smtp.auth", "true");
            prop.setProperty("mail.smtp.host", "mail.example.com");
        }
        if (smtpPort == 25) {
        } else if (smtpPort == 587) {
            log.debug("Going for SSL");
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true");
        }

        prop.setProperty("mail.smtp.port", "" + smtpPort);
        prop.put("mail.smtp.connectiontimeout", "10000");
        prop.put("mail.smtp.timeout", "10000");

        StringTokenizer mailhosts = new StringTokenizer(smtpHost, ", ");
        boolean sent = false;
        while (!sent && mailhosts.hasMoreTokens()) {
            prop.put("mail.smtp.host", smtpHost);
            debug("Sending mail to: " + Arrays.toString(recipents) + " via " + smtpHost);

            try {
                Session session = Session.getDefaultInstance(prop, smtpUser == null || smtpPort == 587 ? null : new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUser, smtpPass);
                    }
                });
                MimeMessage msg = new MimeMessage(session);

                msg.setSentDate(new Date());
                msg.setFrom(new InternetAddress(from));
                InternetAddress[] addresses = new InternetAddress[recipents.length];
                for (int i = 0; i < recipents.length; i++) {
                    addresses[i] = new InternetAddress(recipents[i]);
                }
                msg.setRecipients(Message.RecipientType.TO, addresses);

                if (bccRecipients != null) {
                    InternetAddress[] bccAddresses = new InternetAddress[bccRecipients.length];
                    for (int i = 0; i < bccRecipients.length; i++) {
                        bccAddresses[i] = new InternetAddress(bccRecipients[i]);
                    }
                    msg.setRecipients(Message.RecipientType.BCC, bccAddresses);
                }

                msg.setSubject(subject, "UTF-8");

                MimeMultipart parts = new MimeMultipart();

                MimeBodyPart bodyPart = new MimeBodyPart();
                if (html) {
                    bodyPart.setText(body, "UTF-8");
                    bodyPart.setHeader("Content-Type", "text/html; charset=\"UTF-8\"");
                } else {
                    bodyPart.setText(body);
                }
                parts.addBodyPart(bodyPart);

                for (int i = 0; attachments != null && i < attachments.length; i++) {
                    debug("Adding attachment: '" + attachments[i] + "'");
                    File attFile = attachments[i];
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.setDataHandler(new DataHandler(attFile.toURI().toURL()));
                    attachmentPart.setFileName(attFile.getName());
                    parts.addBodyPart(attachmentPart);
                }
                msg.setContent(parts);

                debug("Sending mail using things of a generally obscure nature: " + msg.toString());

                Transport trans = session.getTransport("smtp");

                debug("Got transport, going for '" + smtpHost + "' and '" + from);
                // TODO: Consider using another user for login (preferably one with password)
                if (smtpPort > 0) {
                    trans.connect(smtpHost, smtpPort, smtpUser != null ? smtpUser : from, smtpPass);
                } else {
                    trans.connect(smtpHost, smtpUser != null ? smtpUser : from);
                }
                debug("Connectied");
                trans.sendMessage(msg, msg.getAllRecipients());
                debug("Message sent");
                trans.close();
                sent = true;
            } catch (MessagingException exception) {
                String message = "Could not send mail from " + from + " to " + Arrays.toString(recipents) + " subject " + subject;
                debug(message);
                log.error(message, exception);
                exception.printStackTrace();
                return;
            } catch (MalformedURLException exception) {
                String message = "Could not send mail from " + from + " to " + Arrays.toString(recipents) + " subject " + subject;
                debug(message);
                log.error(message, exception);
                exception.printStackTrace();
                return;
            } catch (Exception exception) {
                String message = "Could not send mail from " + from + " to " + Arrays.toString(recipents) + " subject " + subject;
                debug(message);
                log.error(message, exception);
                exception.printStackTrace();
                return;
            }

        }

    }
}
