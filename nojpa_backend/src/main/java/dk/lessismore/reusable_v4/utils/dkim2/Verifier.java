/**
 * Java implementation of DKIM/DomainKeys. 
 * Copyright (c) 2008 Mark Boddington (www.badpenguin.co.uk)
 * 
 * This program is licensed under the terms of the GNU GPL version 2.0.
 * The DKIM specification is documented in RFC 4871
 * See: http://www.ietf.org/rfc/rfc4871.txt
 */
package dk.lessismore.reusable_v4.utils.dkim2;


import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Date;
import java.util.Stack;
import java.util.Vector;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


/**
 * This class provides all the necessary interfaces to check and verify a message
 * for either a DKIM or DomainKey signature.
 * 
 * @author Mark Boddington &lt;dk_NO_im@_SP_bad_AM_penguin.co.uk&gt;
 *         <br>http://www.badpenguin.co.uk
 */
public class Verifier {
	
	
	//private Signature sig = null;
	private String sigPref = null;
	//private DkimSignature dkimSig = null;
	//private String mailBody = null;
	//private String mailHeaders = null;
	private boolean leniency = false;
	private NSKeyStore keyStore = null;
	private boolean tryBoth = false;
	private Vector<String> badDomains = null;
	private int maxSigs;
	
	
	/**
	 * Create a new Verifier object. You must supply a NSKeyStore object, a
	 * signature preference, and the chose whether to try both signatures if the
	 * preferred signature is missing. The NSKeyStore will be used to lookup the 
	 * public keys for verifying the Signatures, and if a mail should contain both 
	 * key types, we will use the one you preferred. If we don't find your
	 * preferred signature and you have set arg2 to true then we will try the
	 * other type. 
	 * 
	 * @param keyStore - NSKeyStore
	 * @param sigPref - Signature Preference (DKIM | DomainKey)
	 * @param tryBoth - Try both signature types
	 */
	public Verifier(NSKeyStore keyStore, String sigPref, boolean tryBoth){
		_Verifier(keyStore,sigPref,tryBoth);
		 
	}
	/**
	 * Create a new Verifier object. You must supply a NSKeyStore object, and a
	 * signature preference. Either DKIM or DomainKey. The NSKeyStore will be used
	 * to lookup the public keys for verifying the Signatures, and if a mail should
	 * contain both key types, we will use the one you preferred.
	 * <br><br>
	 * This instance will default to failing over to the other key type, should the
	 * preferred key type be missing. You can change this behaviour by calling
	 * Verifier.tryBoth(false);
	 * 
	 * @param keyStore - NSKeyStore
	 * @param sigPref - Signature Preference (DKIM | DomainKey)
	 */
	public Verifier(NSKeyStore keyStore, String sigPref){
		_Verifier(keyStore,sigPref,true);
	}
	
	/**
	 * Create a new Verifier object. You must supply a NSKeyStore object. This
	 * verifier will default to using the DKIM-Signature, but will try to use a
	 * DomainKey-Signature if a DKIM one is not present.
	 * <br><br>
	 * If you wish to stop this verifier failing back to Domainkey signatures, then
	 * you should call Verifier.tryBoth(false), or use a different constructor.
	 * 
	 * @param keyStore - NSKeyStore
	 */
	public Verifier(NSKeyStore keyStore)  {
		_Verifier(keyStore,"DKIM",true);
	}
	
	/**
	 * Private function called by the Verifier constructors.
	 * 
	 * @param keyStore - NSKeyStore
	 * @param sigPref - Signature Preference (DKIM | DomainKey)
	 * @param tryBoth - Try both signature types
	 */
	private void _Verifier(NSKeyStore keyStore, String sigPref, boolean tryBoth) {
		this.tryBoth = tryBoth;
		this.sigPref = sigPref;
		this.keyStore = keyStore;
		leniency = false;
		badDomains = new Vector<String>();
		maxSigs = 1;
	}
	
	/**
	 * Be more lenient when we encounter broken signatures or errors. Quietly fix what we can.
	 * For example, the DKIM RFC says an empty g tag in DNS will never match any addresses,
	 * therefore it should fail. With leniency set, we will treat g= as though it were g=*. 
	 * 
	 * @param leniency - turn on/off leniency
	 */
	public void setleniency(boolean leniency) {
		this.leniency = leniency;
	}
	
	/**
	 * Set the fail back option on or off. If the signature of the type preferred
	 * is not available in the message, should we attempt to use the other type?
	 * 
	 * @param tryBoth - Try other signatures?
	 */
	public void tryBoth(boolean tryBoth) {
		this.tryBoth = tryBoth;
	} 
	

	/**
	 * Set up the list of bad domains. These are domains which we will not
	 * allow to be used in the d= tag. For example: TLDs such as .co.uk or .com
	 * 
	 * @param domains
	 */
	public void setBadDomains(String[] domains) {
		for ( int i = 0 ; i < domains.length ; i++) {
			badDomains.add(domains[i]);
		}
	}
	
	/**
	 * Add the given domain to the list of Bad Domains. These domains will
	 * be rejected should they appear in the d= tag of a DKIM signature.
	 * 
	 * @param domain
	 */
	public void addBadDomain(String domain) {
		badDomains.add(domain);
	}
	
	/**
	 * The Verifier will default to trying only one Signature, the last one we find
	 * (of you preferred type, if possible). However you can change this behaviour
	 * by increasing the maxSigs variable. If you set it to a number greater than 1,
	 * then should we find an email with more than one Signature, we will try them 
	 * in reverse order, until one verifies, or we hit maxSigs.<br>
	 * 
	 * The minimum value for maxSigs is 1.
	 * 
	 * @param maxSigs - Number of signatures to verify
	 */
	public void setMaximumSigs(int maxSigs) {
		if ( maxSigs < 1)
			maxSigs = 1;
		this.maxSigs = maxSigs;
	}
	
	/**
	 * Get the number of Signatures that this Verifier will attempt to Verify. Use the
	 * setMaximumSigs() method to change this value.
	 * 
	 * @return Number of signatures to verify
	 */
	public int getMaximumSigs() {
		return maxSigs;
	}
	
	
	/**
	 * This method checks the signatures D tag against the bad domain
	 * list. The method returns true if the domain is unlisted, other wise
	 * it returns false. 
	 * 
	 * @param dkimSig
	 * @param isDKIM
	 * @throws DkimException if check fails
	 */
	private void checkBadDomains(DkimSignature dkimSig, boolean isDKIM) throws DkimException {
		String domain = dkimSig.getDtag();
		if ( badDomains.contains(domain)) {
			if ( isDKIM )
				throw new DkimException(DkimError.SIGFAIL, "The message is signed by an untrusted/Bad domain");
			else
				throw new DkimException(DkimError.bad, "The message is signed by an untrusted/Bad domain");
		}
	}
	
	/**
	 * Private function to check the body data against the message digest. if the
	 * digest matches the one found in the signature, then we will return true
	 * 
	 * @param dkimSig
	 * @param Mail Body
	 * @throws DkimException if check fails
	 */
	private void checkBodyHash(DkimSignature dkimSig, String mailBody) throws DkimException {
		MessageDigest md = null;
		
		try {
			if ( dkimSig.getJavaAlg().equals("SHA256withRSA") ) {
				md = MessageDigest.getInstance("SHA-256");
			} else {
				md = MessageDigest.getInstance("SHA-1");
			}
		} catch (NoSuchAlgorithmException e) {
			throw new DkimException(DkimError.LIBERROR, "Java couldn't find the required hash algorithm", e);
		}

		md.update(mailBody.getBytes());
		
		BASE64Encoder bsenc = new BASE64Encoder();
		String digest = bsenc.encode(md.digest());
		
		if ( ! digest.equals(dkimSig.getBodyHash())) {
			throw new DkimException(DkimError.BODYHASH, "The body hash did not verify.");
		}
	}
	
	/**
	 * Check that the signature expires tag (if present) is not in the past.
	 * If the recievedTime is 0, then we will utils against the current time, other
	 * wise we will utils the expire time against the recievedTime value.
	 * 
	 * @param dkimSig
	 * @param receivedTime
	 * @throws DkimException if check fails
	 */
	private void checkExpireTime(DkimSignature dkimSig, long receivedTime) throws DkimException {
		String xtag = dkimSig.getXtag();
		if ( xtag.equals("\0")) {
			return;
		} else {
			if ( xtag.length() > 12 ) {
				throw new DkimException(DkimError.SIGSYNTAX, "The expires tag is > 12 chars");
			}
			
			Long expires = Long.parseLong(xtag);
			String ttag = dkimSig.getTtag();
			
			// RFC says if t and x are present, then x must be greater
			if ( ! ttag.equals("\0") ) {
				Long time = Long.parseLong(ttag);
				if ( time > expires ) {
					throw new DkimException(DkimError.SIGFAIL, "The timestamp tag is newer than the expire tag");
				}
			}
			
			long now = 0;
			if ( receivedTime == 0 ) {
				now = new Date().getTime() / 1000;
			} else {
				now = receivedTime;
			}
			
			if ( (expires - now) < 0) 
				throw new DkimException(DkimError.SIGEXPIRED, "The Signature has expired");
			
		}

	}
	
	/**
	 * private function to check if the granularity for the key (if it exists)
	 * matches the iTag (if it exists). Throws a DkimException with DkimError
	 * set to KEYFAIL, if it's inapplicable.
	 * 
	 * @param dkimSig - A DkimSignature object
	 * @param nsKey - A NSKey object
	 * @throws DkimException if check fails
	 */
	private void checkGranularity(DkimSignature dkimSig, NSKey nsKey) throws DkimException {
		String granularity = nsKey.getGranularity();
		if ( granularity.equals("*"))
			return;
		
		if ( leniency && granularity.isEmpty())
			return;
		
		String iTag = dkimSig.getItag();
		if ( ! iTag.equals("\0")) {
			int local = iTag.indexOf("@");
			if ( local > 0) {
				granularity = granularity.replaceAll("\\*", ".*");
				if ( ! iTag.substring(0, local).matches(granularity))
					throw new DkimException(DkimError.KEYFAIL,"Key Granularity is not applicable for Signature");
			}
		}
	}
	
	/**
	 * Private method for checking if the NSKey restricts the use of subdomains. If use
	 * is restricted then we verify that the I tag matches the D tag exactly. We throw an
	 * exception if the check fails
	 * 
	 * @param dkimSig - The Dkimsignature for this message
	 * @param nsKey - The NSKey for this message
	 * @throws DkimException if check fails
	 */
	private void checkSubdomains(DkimSignature dkimSig, NSKey nsKey) throws DkimException {
		if ( nsKey.noSubdomains() ) {
			String Itag = dkimSig.getItag();
			if ( Itag.equals("\0")) {
				return;
			}
			String domain = Itag.substring(Itag.indexOf('@')+1);
			if ( ! domain.equals(dkimSig.getDtag()))
				throw new DkimException(DkimError.KEYFAIL, "Key can not be used for sub-domains");
		}
	}
	
	/**
	 * Check that the hash algorithm specified in the key matches the hash algorithm
	 * in the signature. Throw a DkimException with DkimError.KEYHASH if they don't
	 * match
	 * 
	 * @param dkimSig - The Dkimsignature for this message
	 * @param nsKey - The NSKey for this message
	 * @throws DkimException on failure
	 */
	private void checkHashAlgorithm(DkimSignature dkimSig, NSKey nsKey) throws DkimException {
		String Atag = dkimSig.getAtag();
		String hashes[] = nsKey.getHashAlgorithm().split(":");
		for ( int i=0; i < hashes.length ; i++) {
			if ( Atag.endsWith(hashes[i]) )
				return;
		}
		throw new DkimException(DkimError.KEYHASH, "The key algorithm does not match the Signature.");
	}
	
	/**
	 * Verify the provided email message. We will scan the message for DKIM and/or
	 * DomainKey signatures and attempt to verify one of them. If we fail to verify the
	 * Signature, then an exception indicating the reason for the failure will be thrown.
	 * <br>
	 * This function uses the systems current time when checking any expire headers in 
	 * the signature. You may set a specific receive time, by using the other verifyMail
	 * method.
	 * 
	 * @param msg - The message to be verified
	 * @throws IOException
	 * @throws DkimException
	 */
	public void verifyMail(InputStream msg) throws IOException, DkimException {
		 verifyMail(msg, 0);
	}
	
	/**
	 * This function performs the same actions as verifyMail(msg), however, this one
	 * will accept a long representing the messages received time. The received time 
	 * will then be used when verifying any expires headers which may be present in 
	 * the signature.
	 * 
	 * @param msg
	 * @param receivedTime
	 * @throws IOException
	 * @throws DkimException
	 */
	public void verifyMail(InputStream msg, long receivedTime) throws IOException, DkimException {
	
		Canonicaliser canon = new Canonicaliser(sigPref);
		MailMessage mail = new MailMessage();
		mail.processMail(msg);
		
		int dkimHeaders = mail.dkimHeaderCount();
		int domkeyHeaders = mail.domkeyHeaderCount();
		
		// If we don't have any usable signatures in the message, then throw a NOSIG, ASAP
		if ( ( dkimHeaders == 0 ) && ( domkeyHeaders == 0 ))
			throw new DkimException(DkimError.NOSIG, "There are no Signatures in this message");
		
		if ( (dkimHeaders == 0) && sigPref.equalsIgnoreCase("DKIM") && (!tryBoth) )
			throw new DkimException(DkimError.NOSIG, "There are no DKIM Signatures in this message");
		
		if ( (domkeyHeaders == 0) && sigPref.equalsIgnoreCase("DomainKey") && (!tryBoth) ) 
			throw new DkimException(DkimError.NOSIG, "There are no DomainKey Signatures in this message");
		
		// Process one or many signatures
		if ( maxSigs == 1) {
			DkimSignature dkimSig = new DkimSignature(canon.initVerify(mail.getHeaders(),tryBoth), leniency);
			processSignature(dkimSig, canon, mail, receivedTime);
		} else {
			int tries = 0;
			DkimException tempFail = null;
			DkimException lastFail = null;
			
			canon.initVerify(mail.getHeaders());
			
			Stack<String> dkimSigs = canon.getDkimHeaders();
			Stack<String> domkeySigs = canon.getDomKeyHeaders();
			
			if ( sigPref.equalsIgnoreCase("DKIM") ) {
				 while ( (! dkimSigs.isEmpty()) && ( tries < maxSigs )) {
					 tries++;
					 try { 
						 DkimSignature dkimSig = new DkimSignature(dkimSigs.pop(),leniency);
						 processSignature(dkimSig, canon, mail, receivedTime);
						 return;
					 } catch ( DkimException dke) {
						 if (dke.getErrorType().equals(ErrorType.TEMPFAIL))
							 tempFail = dke;
						 else
							 lastFail = dke;
					 }
				 }
				 while ( (tryBoth) && (!domkeySigs.isEmpty()) && (tries < maxSigs )) {
					 tries++;
					 try { 
						 DkimSignature dkimSig = new DkimSignature(domkeySigs.pop(),leniency);
						 processSignature(dkimSig, canon, mail, receivedTime);
						 return;
					 } catch ( DkimException dke) {
						 if (dke.getErrorType().equals(ErrorType.TEMPFAIL))
							 tempFail = dke;
						 else
							 lastFail = dke;
					 }
				 }
				 
			} else {
				while ( (! domkeySigs.isEmpty()) && ( tries < maxSigs )) {
					 tries++;
					 try { 
						 DkimSignature dkimSig = new DkimSignature(domkeySigs.pop(),leniency);
						 processSignature(dkimSig, canon, mail, receivedTime);
						 return;
					 } catch ( DkimException dke) {
						 if (dke.getErrorType().equals(ErrorType.TEMPFAIL))
							 tempFail = dke;
						 else
							 lastFail = dke;
					 }
				 }
				 while ( (tryBoth) && (!dkimSigs.isEmpty()) && (tries < maxSigs )) {
					 tries++;
					 try { 
						 DkimSignature dkimSig = new DkimSignature(dkimSigs.pop(),leniency);
						 processSignature(dkimSig, canon, mail, receivedTime);
						 return;
					 } catch ( DkimException dke) {
						 if (dke.getErrorType().equals(ErrorType.TEMPFAIL))
							 tempFail = dke;
						 else
							 lastFail = dke;
					 }
				 }
			}
			
			if ( tempFail != null) 
				 throw tempFail;
			 else
				 throw lastFail;
		}
	}
	
	/**
	 * Perform the message verification work for the various VerifyMail() methods
	 * 
	 * @param dkimSig
	 * @param canon
	 * @param mail
	 * @param receivedTime
	 * @throws IOException
	 * @throws DkimException
	 */
	private void processSignature(DkimSignature dkimSig, Canonicaliser canon, MailMessage mail,
			long receivedTime) throws IOException, DkimException {
		
		Signature sig = null;
		
		String mailHeaders = canon.processHeaders(dkimSig);
		dkimSig.checkValidity();
		
		boolean dkim = dkimSig.isDKIM();
		
		checkExpireTime(dkimSig, receivedTime);
		checkBadDomains(dkimSig, dkim);
		
		String mailBody = canon.processBody(mail.getBody(), dkimSig.getLtag(), dkimSig.getBodyMethod());
		
		NSKey[] nsKeys = keyStore.retrieveKeys(dkimSig.getDnsRecord());
		NSKey nsKey = null;

		// Store errors, so we can concatenate them together, if we have multiple keys.
		String keyErrors = null;
		DkimException dke = null;
		
		for (int i=0; i < nsKeys.length ; i++) {
			try {
				
				// Check the key is OK first, if it isn't an exception will be thrown
				nsKeys[i].getKey();
				
				checkGranularity(dkimSig, nsKeys[i]);

				checkSubdomains(dkimSig, nsKeys[i]);

				checkHashAlgorithm(dkimSig, nsKeys[i]);
				
				nsKey = nsKeys[i];
				break;
			} catch (DkimException e) {
				if (keyErrors == null) {
					dke = e;
					keyErrors = "Key " + i + ": " + e.getMessage();
				} else {
					keyErrors = keyErrors.concat(", Key " + i + ": " + e.getMessage());
					dke = new DkimException(e.getError(), keyErrors);
				}
			}
		}
		
		if (nsKey == null) {
			throw dke;
		}
		
		if ( dkim ) {
			checkBodyHash(dkimSig, mailBody);
		}
		
		BASE64Decoder bs = new BASE64Decoder();
		byte[] sigBuf = bs.decodeBuffer(dkimSig.getBtag());
		
		try {
			sig = Signature.getInstance(dkimSig.getJavaAlg());
			sig.initVerify(nsKey.getKey());
			sig.update(mailHeaders.getBytes());
			
			if ( ! dkim ) {
				sig.update("\r\n".getBytes());
				sig.update(mailBody.getBytes());
			}
			
			if ( sig.verify(sigBuf) ) {
				return;
			} else {
				if (dkim)
					throw new DkimException(DkimError.SIGFAIL,"Message Verification Failed.");
				else 
					throw new DkimException(DkimError.bad, "Message Verification Failed.");
			}
		} catch (NoSuchAlgorithmException n) {
			n.printStackTrace();
		} catch (InvalidKeyException k) {
			if ( dkim )
				throw new DkimException(DkimError.KEYSYNTAX, "The Key found was invalid",k);
			else
				throw new DkimException(DkimError.badformat, "The Key found was invalid",k);
		} catch (SignatureException s) {
			if (dkim)
				throw new DkimException(DkimError.SIGFAIL, "Could not process the signature data",s);
			else
				throw new DkimException(DkimError.badformat, "The Key found was invalid",s);
		}
	}
		
}


