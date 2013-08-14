package dk.lessismore.reusable_v4.utils.dkim2.utils;

import dk.lessismore.reusable_v4.utils.dkim2.DkimException;
import dk.lessismore.reusable_v4.utils.dkim2.ErrorType;
import dk.lessismore.reusable_v4.utils.dkim2.NSKeyStore;
import dk.lessismore.reusable_v4.utils.dkim2.Verifier;

import java.io.FileInputStream;
import java.io.InputStream;

public class VerifyMail {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VerifyMail.class);

    public static boolean verify(InputStream is) {
		try {
			NSKeyStore keyStore = new NSKeyStore();
			keyStore.setKeyLimit(5);
			Verifier ver = new Verifier(keyStore, "DKIM");
			ver.setleniency(true);
			ver.setMaximumSigs(3);

			ver.verifyMail(is);
			log.debug("DKIM: verify: Message OK");
			is.close();
            return true;

		} catch (DkimException de) {
            try { is.close(); } catch (Exception e) { };
            if ( de.getErrorType() == ErrorType.NOSIG) {
				// No DKIM signature in the message, just return true
                log.debug("DKIM: verify: Message OK [NOSIG]");
                return true;
			} else if ( de.getErrorType().equals(ErrorType.TEMPFAIL))  {
				// message failed, but may succeed later
				log.debug("DKIM: verify TEMPFAIL -- " + de.getError().getStatus() + " " + de.getMessage() + " " + de.getError().getDescription(), de);
			} else if ( de.getErrorType() == ErrorType.PERMFAIL) {
				// message failed and will never verify
				log.debug("DKIM: verify PERMFAIL -- " + de.getError().getStatus() + " " + de.getMessage() + " " + de.getError().getDescription(), de);
			} else {
				// Internal Library Error
                log.debug("DKIM: verify LIBRARY ERROR!!! -- " + de.getError().getStatus() + " " + de.getMessage() + " " + de.getError().getDescription(), de);
            }
		} catch ( Exception e) {
			log.debug("DKIM: verity stream exception: " + e.getMessage(), e);
		}
        return false;
    }

	public static boolean verify(String filename) {
        try {
            log.debug("DKIM: verifying file: " + filename);
            return verify(new FileInputStream(filename));
        } catch (Exception e) {
            log.debug("DKIM: verify exception: " + e.getMessage(), e);
        }
        return false;


	}


}
