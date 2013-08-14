/**
 * 
 */
package dk.lessismore.reusable_v4.utils.dkim2.utils;

import dk.lessismore.reusable_v4.resources.PropertyResources;
import dk.lessismore.reusable_v4.utils.dkim2.CanonicalMethod;
import dk.lessismore.reusable_v4.utils.dkim2.DkimException;
import dk.lessismore.reusable_v4.utils.dkim2.ErrorType;
import dk.lessismore.reusable_v4.utils.dkim2.Signer;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.util.SharedByteArrayInputStream;
import java.io.*;
import java.security.PrivateKey;

public class SignMail {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignMail.class);
    private static final CanonicalMethod header = CanonicalMethod.RELAXED;
    private static final CanonicalMethod body = CanonicalMethod.SIMPLE;
    private static final String DKIM_ALG = "rsa-sha256";

    private String domain;
    private String selector;
    private String identifier;
    private FileInputStream store = null;
    private KeyManager km = null;

    protected SignMail(String keyStoreFileName, String keyStorePass, String domain, String selector, String identifier) throws Exception {
        store = new FileInputStream( keyStoreFileName );
        km = new KeyManager(store, keyStorePass);
        this.domain = domain;
        this.selector = selector;
        this.identifier = identifier;
    }

    public MimeMessage sign(Session session, ByteArrayOutputStream baos) throws Exception {
        SharedByteArrayInputStream is = new SharedByteArrayInputStream(baos.toByteArray());
        SharedByteArrayInputStream bis = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            PrivateKey key = km.getKey(selector + "." + domain);
			Signer sig = new Signer(selector, domain, DKIM_ALG, key);
			sig.getDkimSignature().setHeaderMethod(header);
			sig.getDkimSignature().setBodyMethod(body);
			sig.getDkimSignature().setItag(identifier);
			sig.setAutoHeaders(true);
			//sig.setAutoTimeStamp(true);

			sig.signMail(is, bos);
			bis = new SharedByteArrayInputStream(bos.toByteArray());
            MimeMessage msg =  new MimeMessage(session, bis);
            return msg;
		} catch (DkimException d) {
			if ( d.getErrorType() == ErrorType.NOSIG) {
				// No signature, 
			} else if ( d.getErrorType().equals(ErrorType.TEMPFAIL))  {
				// message failed, but may succeed later
				log.debug("TEMPFAIL -- " + d.getError().getStatus());
				log.debug(d.getError().getDescription());
				log.error(d.getMessage(), d);
			} else if ( d.getErrorType() == ErrorType.PERMFAIL) {
				// message failed and will never verify
				log.debug("PERMFAIL -- " + d.getError().getStatus());
				log.debug(d.getError().getDescription());
				log.error(d.getMessage(), d);
			} else {
				// Internal Library Error
				log.debug("Internal Library Error -!!!- " + d.getError().getStatus());
				log.debug(d.getError().getDescription());
				log.error(d.getMessage(), d);
            }
		} catch (Exception e) {
            log.error("got exception while signing: " + e.getMessage(), e);
        } finally {
            if (is != null) {
                try { is.close(); } catch (Exception e) { /* skip it */ }
            }
            if (bis != null) {
                try { bis.close(); } catch (Exception e) { /* skip it */ }
            }
            if (bos != null) {
                try { bos.close(); } catch (Exception e) { /* skip it */ }
            }
        }
        return null;
	}


    public static SignMail getInstance(PropertyResources propertyResources, String smtpUser, String from) {
        try {
            String domainID = smtpUser != null ? smtpUser : from;
            if (domainID == null) {
                throw new Exception("smtpUser and from are null, how can i continue?!");
            }
            String domain = domainID.substring(domainID.indexOf('@') + 1);
            if (propertyResources.gotResource("dkim.sign." + domain)) {
                log.debug("DKIM signing message");
                String selector = propertyResources.getString("dkim.selector." + domain);
                String keyStoreFilename = propertyResources.getString("keyStoreFilename");
                String keyStorePass = propertyResources.getString("keyStorePass");
                if (selector == null || keyStoreFilename == null || keyStorePass == null) {
                    throw new Exception("selector keystoreFilename or keystorePass are null");
                }
                return new SignMail(keyStoreFilename, keyStorePass, domain, selector, domainID);
            } else {
                log.debug("DKIM is not configured for domain: " + domain);
            }
        } catch (Exception e) {
            log.error("got exception while dkimSigning: " + e.getMessage(), e);
        }
        return null;
    }

    public static void sign(PropertyResources propertyResources, String msgFilename, String dkimMsgFilename) {
        File msgFile = new File(msgFilename);
        if (!msgFile.exists()) {
            log.debug("DKIM: not existing input file: " + msgFile.getAbsolutePath());
            return;
        }
        File dkimMsgFile = new File(dkimMsgFilename);
        if (dkimMsgFile.exists()) {
            log.debug("DKIM: removing preexisting dkim msg file: " + dkimMsgFile.getAbsolutePath());
            dkimMsgFile.delete();
        }
        String keyStoreFileName = propertyResources.getString("keyStoreFilename");
        String keyStorePass = propertyResources.getString("keyStorePass");
        FileInputStream store = null;
        if (keyStoreFileName == null || keyStorePass == null) {
            log.debug("DKIM: don't know the keystore or storepass");
            return;
        }
        FileOutputStream fosDkimMsg = null;
        FileInputStream fisMsg = null;

        try {
            String domain = "superjack.dk"; // TODO not hardcoded!!!
            String selector = propertyResources.getString("dkim.selector." + domain);
            if (selector == null) {
                log.debug("DKIM: don't know the selector for domain: " + domain);
                return;
            }
            store = new FileInputStream(keyStoreFileName);
            KeyManager km = new KeyManager(store, keyStorePass);
            PrivateKey key = km.getKey(selector + "." + domain);
			Signer sig = new Signer(selector, domain, DKIM_ALG, key);
			sig.getDkimSignature().setHeaderMethod(header);
			sig.getDkimSignature().setBodyMethod(body);
			// TODO get the identifier from somewhere? the smtpUser? sig.getDkimSignature().setItag(identifier);
			sig.setAutoHeaders(true);
			//sig.setAutoTimeStamp(true);

            fosDkimMsg = new FileOutputStream(dkimMsgFile);
            fisMsg = new FileInputStream(msgFile);
            sig.signMail(fisMsg, fosDkimMsg);
		} catch (DkimException de) {
			if (de.getErrorType() == ErrorType.NOSIG) {
				// No signature, this thing should not happen here
                log.debug("DKIM: NOSIG, can't happen here -- " + de.getError().getStatus() + " " + de.getMessage() + " " + de.getError().getDescription(), de);
			} else if (de.getErrorType().equals(ErrorType.TEMPFAIL))  {
				// message failed, but may succeed later
				log.debug("DKIM: sign TEMPFAIL -- " + de.getError().getStatus() + " " + de.getMessage() + " " + de.getError().getDescription(), de);
			} else if (de.getErrorType() == ErrorType.PERMFAIL) {
				// message failed and will never verify
				log.debug("DKIM: sign PERMFAIL -- " + de.getError().getStatus() + " " + de.getMessage() + " " + de.getError().getDescription(), de);
			} else {
				// Internal Library Error
				log.debug("DKIM: sign LIBRARY ERROR!!! -- " + de.getError().getStatus() + " " + de.getMessage() + " " + de.getError().getDescription(), de);
            }
		} catch (Exception e) {
            log.error("DKIM: got exception while signing: " + e.getMessage(), e);
        } finally {
            if (store != null) {
                try { store.close(); } catch (Exception e) { /* skip it */ }
            }
            if (fisMsg != null) {
                try { fisMsg.close(); } catch (Exception e) { /* skip it */ }
            }
            if (fosDkimMsg != null) {
                try { fosDkimMsg.close(); } catch (Exception e) { /* skip it */ }
            }
        }
    }
}
