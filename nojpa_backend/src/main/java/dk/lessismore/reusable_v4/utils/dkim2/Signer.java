/**
 * Java implementation of DKIM/DomainKeys. 
 * Copyright (c) 2008 Mark Boddington (www.badpenguin.co.uk)
 * 
 * This program is licensed under the terms of the GNu GPL version 2.0.
 * The DKIM specification is documented in RFC 4871
 * See: http://www.ietf.org/rfc/rfc4871.txt
 */
package dk.lessismore.reusable_v4.utils.dkim2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Date;

import sun.misc.BASE64Encoder;


/**
 * The Signer class implements the necessary methods to create a DKIM or DomainKey
 * signature header for a given message. It tries to be compliant with version 1 of
 * the DKIM specification. See: http://www.ietf.org/rfc/rfc4871.txt
 * 
 * @author Mark Boddington &lt;dk_NO_im@_SP_bad_AM_penguin.co.uk&gt;
 *         <br>http://www.badpenguin.co.uk;
 */
public class Signer {
	
	/**
	 * The DKIM Signature Object to use as a template. This object will be cloned for
	 * each message which is signed.
	 */
	private DkimSignature dkimSig = null;
	
	/**
	 * A Java Security Private Key object, used for signing our messages.
	 */
	private PrivateKey privateKey = null;
	
	/**
	 * If this is on, then we will sign the RFC recommended headers we find in each
	 * message. This will always include From:
	 */
	private boolean autoHeaders = false;
	
	/**
	 * When auto headers is in use, the strings held in this vector will be added,
	 * after the RFC recommended H tag has been set.
	 */
	private String[] appendHeaders = null;
	
	/**
	 * Should the Signer, automatically update the signing time?
	 */
	private boolean autoTimeStamp = false;
	
	/**
	 * Should the Signer automatically add an expires tag? 
	 */
	private boolean autoExpire = false;
	
	/**
	 * The time from now, at which the Signature should expire. Default 10 minutes.
	 */
	private int expireSeconds = 600;
	
	/**
	 * Creates a Signer object using the specified Domain, Selector, Algorithm and 
	 * PrivateKey. This constructor also reads in the colon separated list of
	 * headers which you wish to be signed. 
	 * We will use defaults for all other DKIM options, unless you call
	 * one of the methods to modify a setting. 
	 * 
	 * @param selector - The selector for this DKIM header
	 * @param domain - The domain name for this DKIM header
	 * @param headers - A colon separated list of headers to sign
	 * @param alg - The hashing Algorithm to be used, rsa-sha256 or rsa-sha1
	 * @param key - The PrivateKey (which matches the public one found in DNS).
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException
	 */
	public Signer(String selector, String domain, String headers, String alg, PrivateKey key) throws DkimException {	
		dkimSig = new DkimSignature(selector, domain, headers);
		dkimSig.setAtag(alg);
		privateKey = key;
		newSig();
	}
	
	/**
	 * Creates a Signer object using the specified Domain, Selector and 
	 * PrivateKey. We will use defaults for all other DKIM options, unless you call
	 * one of the methods to modify their settings. 
	 * 
	 * @param selector - The selector for this DKIM header
	 * @param domain - The domain name for this DKIM header
	 * @param alg - The hashing Algorithm to be used, rsa-sha256 or rsa-sha1
	 * @param key - The PrivateKey (which matches the public one found in DNS).
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException
	 */
	public Signer(String selector, String domain, String alg, PrivateKey key) throws DkimException  {
		dkimSig = new DkimSignature(selector, domain);
		dkimSig.setAtag(alg);
		privateKey = key;
		newSig();
	}
	
	
	/**
	 * Create a Signer object using the specified DkimSignature object, and the 
	 * PrivateKey. This version of the constructor allows you to control all of the
	 * settings of the DKIM signature..... Because you create it yourself ;-)
	 * 
	 * @param DKIMSig
	 * @param key
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws DkimException
	 */
	public Signer(DkimSignature DKIMSig, PrivateKey key) throws DkimException {
		dkimSig = DKIMSig;
		privateKey = key;	
		newSig();
	}
	
	/**
	 * Generate a new signature object to be used to generate the DKIM
	 * Signature hash
	 * 
	 * @return Signature object
	 * @throws DkimException
	 */
	private Signature newSig() throws DkimException {
		Signature mailSig = null;
		try {
			mailSig = Signature.getInstance(dkimSig.getJavaAlg());
			mailSig.initSign(privateKey);
		} catch (NoSuchAlgorithmException e) {
			throw new DkimException(DkimError.LIBERROR,"Failed to find specified algorithm",e);
		} catch (InvalidKeyException e) {
			throw new DkimException(DkimError.LIBERROR,"Invalid key specified",e);
		}
		return mailSig;
	}
	
	/**
	 * This private function is used by the signMail method to generate the body
	 * hash. It expects to be given the mail body, and returns a BASE64 encoded
	 * digest of the data.
	 * 
	 * @param mailBody - The body data of the mail message
	 * @return BASE64 encoded digest
	 */
	private String genBodyHash(String mailBody, DkimSignature dks) throws DkimException {
		
		MessageDigest md = null;
		
		try {
			if ( dks.getJavaAlg().equals("SHA256withRSA") ) {
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
		
		return digest;
	}
	
	/**
	 * If autoHeaders is on (true), then we will ask the canoncialiser to provide us
	 * with the list of headers present in the message, and recommended by the RFC.
	 * We will then sign those headers, this will override any headers which were
	 * manually set on the DkimSignature object.
	 * 
	 * @param autoHeaders
	 */
	public void setAutoHeaders(boolean autoHeaders ) {
		this.autoHeaders = autoHeaders;
	}
	
	/**
	 * Get the current autoHeaders setting for this Signer.
	 * 
	 * @return autoHeaders true/false
	 */
	public boolean getAutoHeaders() {
		return autoHeaders;
	}
	
	/**
	 * This method allows you to append extra headers to the ones generated by
	 * autoheaders, and should be used in place of the DkimSignature equivalents
	 * when the Signer has autoHeaders switched on.
	 *  
	 * @param headers
	 */
	public void setAdditionalHeaders(String[] headers) {
		appendHeaders = headers;
	}
	
	/**
	 * Get the current additional headers, which will appended to the H tag, and
	 * included in the signature. These are appended after the Headers which are
	 * detected by autoHeaders, and are only applied if autoHeaders is in use.
	 * 
	 * @return An array containing the headers
	 */
	public String[] getAdditionalHeaders() {
		return appendHeaders;
	}
	
	/**
	 * Set the number of seconds the DkimSignature will be valid for. When
	 * the Signature is updated, this value will be added to the current time
	 * and used to generate the e(X)pires tag in the DKIM Signature.<br>
	 * <br>
	 * Note: This is only used if autoExpire is on, See: setAutoExpire()
	 * 
	 * @param seconds
	 */
	public void setExpireSeconds(int seconds) {
		expireSeconds = seconds;
	}
	
	/**
	 * Get the current value of the Expire Seconds. This is the Validity period
	 * of a newly signed message.
	 * 
	 * @return expireSeconds
	 */
	public int getExpireSeconds() {
		return expireSeconds;
	}
	
	/**
	 * Switch time stamp generation on or off. 
	 * 
	 * @param autoTimeStamp
	 */
	public void setAutoTimeStamp(boolean autoTimeStamp) {
		this.autoTimeStamp = autoTimeStamp;
		if ( ! autoTimeStamp )
			dkimSig.setTtag("\0");
	}
	
	/**
	 * Get the current setting for autoTimeStamp.
	 * 
	 * @return TimeStamp on/off
	 */
	public boolean getAutoTimeStamp() {
		return autoTimeStamp;
	}
	
	/**
	 * Set whether the signature should automatically calculate an expiration tag.
	 * If set then the value of expireSeconds will be used to calculate the value
	 * for the DKIM e(X)pire tag. See the setExpireSeconds() method.
	 * 
	 * @param autoExpire on/off
	 */
	public void setAutoExpire(boolean autoExpire) {
		this.autoExpire = autoExpire;
		if ( ! autoExpire )
			dkimSig.setXtag("\0");
	}
	
	/**
	 * Get the DkimSignature object in use by this Signer.
	 * @return DkimSignature
	 */
	public DkimSignature getDkimSignature() {
		return dkimSig;
	}
	
	/**
	 * Generate a DKIM-Signature header for the provided mail message. This function
	 * expects to receive a BufferedInputStream containing the raw email message. 
	 * That means both the headers and the body as transfered in SMTP data, 
	 * excluding the ending &lt;CRLF&gt;.&lt;CRLF&gt;
	 * 
	 * @param msg - The raw email message (headers + body)
	 * @return DKIM-Signature
	 */
	public String signMail(InputStream msg) throws DkimException {
		
		MailMessage mail = new MailMessage();
		mail.processMail(msg);

		DkimSignature dks = worker(mail);
				
		return dks.genDkimSig();
	}

	/**
	 * Process the incoming email from the InputStream msg and return a DKIM-Signed copy of
	 * the email in the OutputStream out. If anything goes wrong, then throw a DkimException
	 * 
	 * @param msg - The incoming message
	 * @param out - An output stream to write the signed message to
	 * @throws DkimException
	 */
	public void signMail(InputStream msg, OutputStream out) throws DkimException {
		
		MailMessage mail = new MailMessage();
		mail.processMail(msg);
		
		DkimSignature dks = worker(mail);
		
		try {	
			out.write(mail.getHeaders().toByteArray());
			out.write(dks.genDkimSig().getBytes());
			out.write("\r\n".getBytes());
			out.write(mail.getBody().toByteArray());
			out.flush();
		} catch (IOException e) {
			throw new DkimException(DkimError.LIBERROR,"IOException occurred while writing message",e);
		}
		
	}
	
	/**
	 * Private function which performs the signing actions on behalf of the signMail and
	 * generateSignature functions. We return a DkimSignature object for the incoming mail
	 * 
	 * @param mail - The mail message
	 * @return A DkimSignature for the mail message
	 * @throws DkimException
	 */
	private DkimSignature worker(MailMessage mail) throws DkimException {
		
		DkimSignature dks = null;
		try {
			dks = dkimSig.clone();
		} catch (CloneNotSupportedException e) {
			throw new DkimException(DkimError.LIBERROR,"Internal error, DKIM Signature clone failed", e);
		}

		Signature sig = newSig();
		Canonicaliser canon = new Canonicaliser();
        canon.initSign(mail.getHeaders());
		String mailBody = canon.processBody(mail.getBody(), dks.getLtag(), dks.getBodyMethod());
		
		String digest = genBodyHash(mailBody,dks);
		dks.setBHtag(digest);
		
		if ( autoHeaders ) {
			dks.setHtag(canon.getRecommendedHeaders());
			if ( appendHeaders != null ) {
				for ( int i=0; i < appendHeaders.length ; i++)
					dks.addHeader(appendHeaders[i]);
			}
		}
		
		long now = new Date().getTime() / 1000;
		
		if ( autoTimeStamp ) {
			dks.setTtag(Long.toString(now));
		}
		
		if ( autoExpire ) {
			dks.setXtag(Long.toString( ( now + expireSeconds )  ));
		}
		
		String mailHeaders = canon.processHeaders(dks);
		
		try {
			sig.update(mailHeaders.getBytes());
			BASE64Encoder bsenc = new BASE64Encoder();
			dks.setBtag(bsenc.encode(sig.sign()));
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return dks;
		
	}

}
