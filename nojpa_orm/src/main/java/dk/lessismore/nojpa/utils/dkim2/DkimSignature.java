/**
 * Java implementation of DKIM/DomainKeys. 
 * Copyright (c) 2008 Mark Boddington (www.badpenguin.co.uk)
 * 
 * This program is licensed under the terms of the GNU GPL version 2.0.
 * The DKIM specification is documented in RFC 4871
 * See: http://www.ietf.org/rfc/rfc4871.txt 
 */
package dk.lessismore.nojpa.utils.dkim2;

import org.apache.commons.codec.binary.Base64;

import java.util.Scanner;
import java.util.regex.Pattern;


/**
 * This class represents a DKIM or DomainKey signature. It has methods to manipulate
 * all of the Tags associated with signature and generate the signature string from
 * the tag settings.
 * 
 * @author Mark Boddington &lt;dk_NO_im@_SP_bad_AM_penguin.co.uk&gt;
 *         <br>http://www.badpenguin.co.uk
 */
public class DkimSignature implements Cloneable {
	
	/**
	 * DKIM Length tag. Should we limit the length of body data for signing? 
	 * The RFC says we should default to signing the whole message.
	 * If value is less than 0 we will omit the tag and sign the whole message.
	 * RFC OPTIONAL (will default to entire body).
	 */ 
	private long sigLtag = -1;
	
	/**
	 *  DKIM Version tag. currently hard coded to 1. The RFC says this version 
	 *  number should always be treated as a string. Eg 1 != 1.0
	 *  RFC REQUIRED.
	 */
	private String sigVtag = "1"; 
	
	/**
	 * DKIM Algorithm tag. This can be either rsa-sha1 or rsa-sha256. 
	 * RFC REQUIRED
	 */
	private String sigAtag = "\0"; 
	
	/**
	 * DKIM Signature tag. The B tag contains the base64 encoded signature data. This includes
	 * the DKIM-Signature header itself, and is created after the BH, so it signs the body
	 * hash data too.
	 * RFC REQUIRED.
	 */
	private String sigBtag = "\0"; 
	
	/**
	 * DKIM Body Hash tag. The hash of the canonicalised body data.
	 * RFC REQUIRED
	 */
	private String sigBHtag = "\0";
	
	/**
	 * DKIM Canonicalisation tag. Which canon method should we use for the header and
	 * the body when signing and verifying the message. The format is "header/body".
	 * RFC OPTIONAL (will default to simple/simple).
	 */
	private String sigCtag = "simple/simple"; 
	
	/**
	 * DKIM Domain tag. This is the domain which is signing the message.
	 * RFC REQUIRED.
	 */
	private String sigDtag = "\0"; 
	
	/**
	 * DKIM Headers tag. This is a list of headers, which should be signed and verified. It 
	 * MUST include at least the "From" header, but SHOULD include others. The verifier may
	 * fail any message that does not sign a reasonable set of headers.
	 * RFC REQUIRED.
	 */
	private String sigHtag = "\0";
	
	/**
	 * DKIM Identity tag. This is the identity for which the mail is being signed. 
	 * RFC OPTIONAL (will default to "@sigDtag").
	 */
	private String sigItag = "\0"; 
	
	/**
	 * The DKIM Query tag. Currently only dns/txt is supported, and is hard coded.
	 * RFC OPTIONAL (will default to dns/txt).
	 */
	private String sigQtag = "dns/txt"; 
	
	/**
	 * DKIM Selector tag. The selector identifies the txt record which holds the key
	 * information for verifying this message. 
	 * RFC REQUIRED
	 */
	private String sigStag = "\0"; 
	
	/**
	 * DKIM Time tag. The time at which this message was signed. 
	 * Unix timestamp as string, no longer than 12 chars.
	 * RFC RECOMMENDED (will default to unknown).
	 */
	private String sigTtag = "\0";
	
	/**
	 * DKIM Expiration tag. The time at which the signature on this message expires. 
	 * Unix timestamp as string, no longer than 12 chars.
	 * RFC RECOMMENDED (will default to no expiration).
	 */
	private String sigXtag = "\0";
	
	/**
	 * DKIM Copied Headers tag. A copy of the some of the original headers, separated by the
	 * pipe "|" symbol, and "|" in the headers/values must be converted to %x7C. 
	 * RFC OPTIONAL (will default to null).
	 */
	private String sigZtag = "\0";
	
	/**
	 * Is this a DKIM or DomainKey Signature?
	 */
	private boolean isDKIM = false;

	/**
	 * Our currently compiled DKIM signature;
	 */
	private String dkimSig = null;
	
	/**
	 * Create a DkimSignature object from an existing Dkim-Signature. This is the
	 * recommended way to instantiate a DkimSignature object for verification.
	 * <br><br>
	 * You SHOULD be strict when verifying signatures, but if you would like us to
	 * assume defaults for mandatory parameters which may be missing, then set leniency
	 * to true.<br><br>
	 * If you are intending to sign an object then you should use one of the other 
	 * constructors as they sets mandatory flags to sensible values.
	 * @param sig - The DKIM-Signature
	 * @param leniency - Use defaults for missing mandatory tags?
	 * 
	 * @throws DkimException
	 */
	public DkimSignature(String sig, boolean leniency) throws DkimException {
		if ( sig.toLowerCase().startsWith("domainkey-signature")) {
			isDKIM = false;
			dkimSig = sig;
		} else if (sig.toLowerCase().startsWith("dkim-signature") ) {
			isDKIM = true;
			dkimSig = sig;
		} else {
			throw new DkimException(DkimError.LIBERROR,"The provided signature is neither DKIM, nor DomainKey?");
		}
		
		if ( leniency ) {
			resetDefaultTags();
		} else {
			resetTags();
		}
		
		updateTagsfromSig();
	}
	
	/**
	 * Construct a DkimSignature with the provided selector, domain and header tags.
	 * <br>
	 * This Signature object will be created for DKIM, and will set all optional 
	 * flags which have defaults to their defaults. The mandatory flags with no
	 * defaults will be set to the values you specify.
	 * 
	 * @throws DkimException
	 */
	public DkimSignature(String selector, String domain, String headers) throws DkimException {
		isDKIM = true;
		resetDefaultTags();
		sigHtag = headers;
		sigStag = selector;
		sigDtag = domain;
		
		updateSigfromTags();
	}
	
	/**
	 * Construct a DkimSignature with the provided selector and domain tags. The
	 * header tag will be set to "from:to:subject:message-id".
	 * <br>
	 * This Signature object will be created for DKIM, and will set all optional 
	 * flags which have defaults to their defaults. The mandatory flags with no
	 * defaults will be set to the values you specify. 
	 * @param selector
	 * @param domain
	 * @throws DkimException
	 */
	public DkimSignature(String selector, String domain) throws DkimException {
		isDKIM = true;
		resetDefaultTags();
		sigHtag = "from:to:subject:message-id";
		sigStag = selector;
		sigDtag = domain;
		
		updateSigfromTags();
	}

	/**
	 * Allow this DkimSignature object to be cloned 
	 */
	public DkimSignature clone() throws CloneNotSupportedException {
		return (DkimSignature) super.clone();
	}
	
	/**
	 * Update the internal DKIM-Signature string to match the values currently set
	 * in the Tags.
	 */
	public void updateSigfromTags() {
		
		if ( isDKIM ) {
			dkimSig = "DKIM-Signature: ";
			dkimSig += "v=" + sigVtag +"; a=" + sigAtag + "; c=" + sigCtag + ";\r\n";
			dkimSig += "\td=" + sigDtag + "; s=" + sigStag;
			if ( sigItag == "\0" ) {
				dkimSig += ";\r\n";
			} else {
				dkimSig += "; i=" + sigItag + ";\r\n";
			}
			if ( sigLtag > -1) {
				dkimSig += "\tl=" + sigLtag + ";\r\n";
			}
			if ( sigXtag != "\0" && sigTtag != "\0" ) {
				dkimSig += "\tt=" + sigTtag + "; x=" + sigXtag + ";\r\n";
			} else if ( sigTtag != "\0") {
				dkimSig += "\tt=" + sigTtag + ";\r\n";
			} else if ( sigXtag != "\0" ) {
				dkimSig += "\tx=" + sigXtag + ";\r\n";
			}

			dkimSig += "\th=" + sigHtag + ";\r\n";
			dkimSig += "\tbh=" + sigBHtag + ";\r\n";
			if ( sigBtag == "\0" ) {
				dkimSig += "\tb=;";
			} else {
				dkimSig += "\tb=" + sigBtag.replaceAll("[\r\n]+", "\r\n\t") +";\r\n";
			}
		} 
		
	}
	
	/**
	 * Update the internal Tags to match the values currently set in the internal
	 * DKIM or DomainKey signature string.
	 * @throws DkimException
	 */
	private void updateTagsfromSig() throws DkimException {

		Scanner dkim = new Scanner(dkimSig);
		dkim.useDelimiter(Pattern.compile(";"));
		dkim.skip(".*?:");
		
		while (dkim.hasNext()) {
			
			String[] tag = dkim.next().trim().split("=",2);
			//System.err.println("Tag: " + tag[0] + ", " + tag[1] );

			if ( tag[0].equalsIgnoreCase("a")) {
				sigAtag = tag[1];
				if ( ! sigAtag.equalsIgnoreCase("rsa-sha256") && 
						! sigAtag.equalsIgnoreCase("rsa-sha1") ) {
					throw new DkimException(DkimError.SIGFAIL, "Message uses un-supported algorithm. Algorithm: " + sigAtag);
				}
			} else if ( tag[0].equalsIgnoreCase("v")) {
				sigVtag = tag[1];
				if (! sigVtag.equals("1")) {
					throw new DkimException(DkimError.SIGVERSION, "Message uses un-supported version of DKIM. Version: " + sigVtag);
				}
			} else if ( tag[0].equalsIgnoreCase("c")) {
				
				
				
				if ( isDKIM ){
					String canon[] = tag[1].split("/");
					if ( canon.length == 1 || canon.length == 2 ) {
						if ( ! canon[0].equalsIgnoreCase("simple") &&
								! canon[0].equalsIgnoreCase("relaxed") ) {
							throw new DkimException(DkimError.SIGFAIL,"Message uses un-supported canonicalisation method. Method: " + canon[0]);
						}
						if ( canon[0].equalsIgnoreCase("relaxed")) {
							this.setMethod(CanonicalMethod.RELAXED);
						} else {
							this.setMethod(CanonicalMethod.SIMPLE);
						}
						if ( canon.length == 2) {
							if ( ! canon[1].equalsIgnoreCase("simple") &&
									! canon[1].equalsIgnoreCase("relaxed") ) {
								throw new DkimException(DkimError.SIGFAIL,"Message uses un-supported canonicalisation method. Method: " + canon[1]);
							}
							if ( canon[1].equalsIgnoreCase("relaxed")) {
								this.setBodyMethod(CanonicalMethod.RELAXED);
							} else {
								this.setBodyMethod(CanonicalMethod.SIMPLE);
							}
						}
					} else {
						throw new DkimException(DkimError.SIGFAIL,"Message uses un-supported canonicalisation method. Methods: " + canon.toString());
					}
				} else {
					if ( ! tag[1].equalsIgnoreCase("simple") &&
							! tag[1].equalsIgnoreCase("nofws")) {
						throw new DkimException(DkimError.badformat, "Invalid canonicalization specified");
					} else {
						if ( tag[1].equalsIgnoreCase("nofws")) {
							this.setMethod(CanonicalMethod.NOFWS);
						} else {
							this.setMethod(CanonicalMethod.SIMPLE);
						}
					}
				}
					
			} else if ( tag[0].equalsIgnoreCase("d")) {
				sigDtag = tag[1];
			} else if ( tag[0].equalsIgnoreCase("s")) {
				sigStag = tag[1];
			} else if ( tag[0].equalsIgnoreCase("h")) {
				sigHtag = tag[1];
			} else if ( tag[0].equalsIgnoreCase("bh")) {
				sigBHtag = tag[1].replaceAll("\\p{javaWhitespace}", "");
			} else if ( tag[0].equalsIgnoreCase("b")) {
				sigBtag = tag[1].replaceAll("\\p{javaWhitespace}", "");
			} else if ( tag[0].equalsIgnoreCase("l")) {
				// RFC says the L tag can be 76 decimal digits, I think 10 is plenty.
				if ( tag[1].length() > 10 ) {
					throw new DkimException(DkimError.LIBERROR,"JavaDKIM limits message length tags at 2GB");
				}
				try {
					sigLtag = Long.parseLong(tag[1]);
				} catch (NumberFormatException nfe) {
					throw new DkimException(DkimError.badformat,"Failed to parse (l)ength tag");
				}
			} else if ( tag[0].equalsIgnoreCase("i")) {
				sigItag = tag[1];
			} else if ( tag[0].equalsIgnoreCase("q")) {
				sigQtag = tag[1];
				if ( isDKIM && ! sigQtag.equals("dns/txt")) {
					throw new DkimException(DkimError.SIGFAIL,"Message uses un-supported query method. Method: " + sigQtag);
				} else if ( ! isDKIM && ! sigQtag.equals("dns")) {
					throw new DkimException(DkimError.badformat,"Message uses un-supported query method. Method: " + sigQtag);
				}
			} else if ( tag[0].equalsIgnoreCase("t")) {
				sigTtag = tag[1];
				if ( ! sigTtag.matches("[0-9]{1,12}")) {
					throw new DkimException(DkimError.SIGFAIL,"Message has an invalid timestamp. Message creation time: " + sigTtag);
				}
			} else if (tag[0].equalsIgnoreCase("x")) {
				sigXtag = tag[1];
				if ( ! sigXtag.matches("[0-9]{1,12}")) {
					throw new DkimException(DkimError.SIGFAIL,"Message has an invalid timestamp. Message creation time: " + sigXtag);
				}
			} else if ( tag[0].equalsIgnoreCase("z")) {
				sigZtag = tag[1];
			}
		}
	}
	
	public void checkValidity() throws DkimException {
		if ( isDKIM )
			checkDKIMValidity();
		else
			checkDomKeyValidity();
	}
	
	
	private void checkDKIMValidity() throws DkimException {
		if ( ! sigVtag.equals("1") ) {
			throw new DkimException(DkimError.SIGVERSION,"Incompatable Version");
		}
		if ( sigAtag.equals("\0") || sigBtag.equals("\0") || sigBHtag.equals("\0")
				|| sigDtag.equals("\0") || sigHtag.equals("\0") 
				|| sigStag.equals("\0")) {
			throw new DkimException(DkimError.SIGREQTAG,"signature missing required tag");
		}
		if ( ! sigHtag.toLowerCase().contains("from")) {
			throw new DkimException(DkimError.SIGFROM,"From field not signed");
		}
		
		if ( ! sigItag.equals("\0") ) {
			if ( ! sigItag.replaceAll(".*@(.*)", "$1").contains(sigDtag) )
				throw new DkimException(DkimError.SIGDOMAIN,"Domain mismatch");
		}
		
	}
	
	private void checkDomKeyValidity() {
		
	}
	
	/**
	 * Get the current value of the (A)lgorithm tag
	 * @return The A tag
	 */
	public String getAtag() {
		return sigAtag;
	}
	
	/**
	 * Set the (A)lgorithm tag to the specified value (must be either "rsa-sha1" or 
	 * "rsa-sha256")
	 * 
	 * @param arg - The Algorithm to use
	 * @throws DkimException - If an invalid algorithm is specified.
	 */
	public void setAtag(String arg) throws DkimException {
		if ( arg.equalsIgnoreCase("rsa-sha1")) {
			sigAtag = "rsa-sha1";
		} else if ( arg.equalsIgnoreCase("rsa-sha256")) {
			sigAtag = "rsa-sha256";
		} else {
			throw new DkimException(DkimError.LIBERROR, "Invalid Algorithm specified");
		}
	}
	
	/**
	 * Get the current value of the (B)ase64 Signature Data
	 * @return The Base64 encoded signature data.
	 */
	public String getBtag() {
		return sigBtag;
	}
	
	/**
	 * Set the (B)ase64 encoded signature data to the specified value.
	 * @param base64 - Base64 encoded signature data.
	 */
	public void setBtag(String base64) {
		sigBtag = base64;
	}
	
	/**
	 * Encode and set the signature data to the specified byte[] value.
	 * @param data - byte array to be encoded to Base64.
	 */
	public void setBtag(byte[] data) {
		sigBtag = Base64.encodeBase64String(data);
	}
	
	/**
	 * Get the current value of the (B)ody (H)ash tag
	 * @return The Base64 encoded BH tag
	 */
	public String getBHtag() {
		return sigBHtag;
	}
	
	/**
	 * Set the (B)ody (H)ash tag to the specified base64 value
	 * @param base64 - base64 encoded Body Hash
	 */
	public void setBHtag(String base64) {
		sigBHtag = base64;
	}
	
	/**
	 * Encode and set the (B)ody (H)ash tag to the specified byte[] value
	 * @param data - byte array to be encoded to Base64.
	 */
	public void setBHtag(byte[] data) {
		sigBHtag = Base64.encodeBase64String(data);
	}
	
	/**
	 * Get the current value of the (V)ersion tag
	 * @return The V tag
	 */
	public String getVtag() {
		return sigVtag;
	}
	
	/**
	 * Set the (V)ersion tag to the specified version.<br>
	 * WARNING: Currently this can only be "1", you don't need to use this (yet)!
	 * @param version
	 */
	public void setVtag(String version) {
		sigVtag = version;
	}
	
	/**
	 * Get the current value of the (C)anonicalisation tag
	 * @return The C Tag
	 */
	public String getCtag() {
		return sigCtag;
	}
	
	/**
	 * Get the current value of the (L)ength tag
	 * @return The L Tag
	 */
	public long getLtag() {
		return sigLtag;
	}
	
	/**
	 * Set the body (L)ength Tag. The body data will be truncated at the length
	 * specified for signing/verifying.<br>
	 * Setting this value to -1 will sign the entire body (the default). A setting
	 * of 0 will sign "CRLF". 
	 * @param length - The bytes of body data to sign.
	 * 
	 */
	public void setLtag(long length) {
		sigLtag = length;
	}
	
	/**
	 * Get the current value of the (H)eaders tag.
	 * @return The H tag - A colon separated list of headers
	 */
	public String getHtag() {
		return sigHtag;
	}
	
	/**
	 * Set the (H)eaders tag to the specified value.<br>
	 * The headers, should be lowercase, and they should be colon separated.
	 * @param headers - The colon separate list of headers to set.
	 */
	public void setHtag(String headers) throws DkimException {
		if ( ! headers.contains("from") ) {
			throw new DkimException(DkimError.LIBERROR, "The H tag must include the \"From\" header!");
		}
		sigHtag = headers;
	}
	
	/**
	 * Get the current value of the (I)dentity tag 
	 * @return The I tag
	 */
	public String getItag() {
		return sigItag;
	}
	
	/**
	 * Set the (I)dentity tag to the specified value.
	 * @param arg0 - The I tag
	 */
	public void setItag(String arg0) {
		sigItag = arg0;
	}
	
	/**
	 * Get the current value of the (D)omain tag.
	 * @return The D tag
	 */
	public String getDtag() {
		return sigDtag;
	}
	
	/**
	 * Set the (D)omain tag to the specified value.<br>
	 * The domain specifes here should have a subdomain of _domainkey, which holds
	 * the selectors in use.
	 * @param domain - The domain for this message
	 */
	public void setDtag(String domain) {
		sigDtag = domain;
	}
	
	/**
	 * Get the current value of the (S)elector tag.
	 * @return The S tag
	 */
	public String getStag() {
		return sigStag;
	}
	
	/**
	 * Set the (S)elector tag to the specified value.
	 * @param selector - The Selector
	 */
	public void setStag(String selector) {
		sigStag = selector;
	}
	
	/**
	 * Get the current value of the (T)imestamp tag.
	 * @return The timestamp tag
	 */
	public String getTtag() {
		return sigTtag;
	}
	
	/**
	 * Set the (T)imestamp tag to the specified value.
	 * @param sigTtag - The timestamp tag
	 */
	public void setTtag(String sigTtag) {
		this.sigTtag = sigTtag;
	}
	
	/**
	 * Get the current value of the e(X)pires tag.
	 * @return The X tag
	 */
	public String getXtag() {
		return sigXtag;
	}
	
	/**
	 * Set the e(X)pires tag to the specified value.
	 * @param sigXtag - The eXpires Tag
	 */
	public void setXtag(String sigXtag) {
		this.sigXtag = sigXtag;
	}
	
	/**
	 * Add the specified header to the H tag.<br>You should specify just the header
	 * name without the colon separator. Although this method will remove any colons
	 * and attempt to format the header correctly.
	 * 
	 * @param header - The header to add.
	 */
	public void addHeader(String header) {
		sigHtag += ":" + header.replaceAll(":","").toLowerCase();
	}
	
	/**
	 * Get the DNS record. The S tag (dot) _domainkey (dot) D tag.
	 * @return the DNS record to lookup
	 * @throws DkimException If the Q tag doesn't specify DNS.
	 */
	public String getDnsRecord() throws DkimException {
		if ( sigQtag.contains("dns"))
			return sigStag + "._domainkey." + sigDtag;
		else
			throw new DkimException(DkimError.LIBERROR,"The Query tag does not specify a DNS record");
	}
	
	/**
	 * Get the JAVA version of the (A)lgorithm tag (eg rsa-sha1 == SHA1withRSA)
	 * @return The Java Algorithm name
	 */
	public String getJavaAlg() {
		if ( sigAtag.equals("rsa-sha256")) {
			return "SHA256withRSA";
		} else {
			return "SHA1withRSA";
		}
	}
	
	/**
	 * Get the (A)lgorithm tag. This is the same as getAtag().
	 * @return The A tag
	 */
	public String getAlgorithm() {
		return sigAtag;
	}
	
	/**
	 * Get the body hash data. This is the same as getBHtag().
	 * @return The BH tag (Base64 encoded body hash).
	 */
	public String getBodyHash() {
		return sigBHtag;
	}
	
	/**
	 * Get the message signature data. This is the same as getBtag()
	 * @return - The B tag (Base64 encoded signature)
	 */
	public String getMessageSignature() {
		return sigBtag;
	}
	
	/**
	 * Return true if this signature is DKIM, false if it is DomainKey
	 * @return is DKIM?
	 */
	public boolean isDKIM() {
		return isDKIM;
	}
	
	/**
	 * Get the canonicalisation that should be used for processing the headers.
	 * @return - simple, relaxed, or nofws
	 */
	public CanonicalMethod getHeaderMethod() {
		if ( sigCtag.startsWith("simple")) {
			return CanonicalMethod.SIMPLE;
		} else if ( sigCtag.startsWith("relaxed")) {
			return CanonicalMethod.RELAXED;
		} else {
			return CanonicalMethod.NOFWS;
		}
	}
	
	/**
	 * Get the canonicalisation that should be used for processing the body.
	 * @return - simple, relaxed, or nofws
	 */
	public CanonicalMethod getBodyMethod() {
		if ( sigCtag.contains("/simple")) {
			return CanonicalMethod.SIMPLE;
		} else if ( sigCtag.contains("/relaxed")) {
			return CanonicalMethod.RELAXED;
		} else
			return getHeaderMethod();
	}
	
	/**
	 * Set the canonicalisation method which should be used for processing the
	 * headers.<br>Throw a DkimError if we are a DomainKey signature, because you
	 * can't specify defferent encodings for header and body when using DomainKey
	 * @param method - The Canonicalisation method
	 * @throws DkimException
	 */
	public void setHeaderMethod(CanonicalMethod method) throws DkimException {
		if ( ! isDKIM )
			throw new DkimException(DkimError.LIBERROR,"You can not specify differing body/header canonicalisation on a DomainKey signature");
		
		String[] canon = sigCtag.split("/");
		if ( canon[1] == null)
			canon[1] = canon[0];
		if ( method.equals(CanonicalMethod.RELAXED) ) {
			sigCtag = "relaxed/" + canon[1];
		} else if ( method.equals(CanonicalMethod.SIMPLE) ) {
			sigCtag = "simple/" + canon[1];
		} else {
			throw new DkimException(DkimError.LIBERROR,"Invalid canonical method for DKIM signature");
		}
		
	}
	
	/**
	 * Set the canonicalisation method which should be used for processing the
	 * body.<br>Throw a DkimError if we are a DomainKey signature, because you
	 * can't specify defferent encodings for header and body when using DomainKey
	 * @param method - The Canonicalisation method
	 * @throws DkimException
	 */
	public void setBodyMethod(CanonicalMethod method) throws DkimException {
	
		if ( ! isDKIM )
			throw new DkimException(DkimError.LIBERROR,"You can not specify differing body/header canonicalisation on a DomainKey signature");
		
		String[] canon = sigCtag.split("/");
		if ( canon[1] == null)
			canon[1] = canon[0];
		
		if ( method.equals(CanonicalMethod.RELAXED) ) {
			sigCtag = canon[0] + "/relaxed";
		} else if ( method.equals(CanonicalMethod.SIMPLE) ){
			sigCtag = canon[0] + "/simple";
		} else {
			throw new DkimException(DkimError.LIBERROR,"Invalid canonical method for DKIM signature");
		}
	}
	
	/**
	 * Set both the body and header canonicalisation method to the specified value
	 * @param method (simple|nofws|relaxed)
	 * @throws DkimException
	 */
	public void setMethod(CanonicalMethod method) throws DkimException {
		if ( ! isDKIM && method.equals(CanonicalMethod.RELAXED)) {
			throw new DkimException(DkimError.LIBERROR,"You can not set relaxed canonicalisation on a DomainKey signature");
		} else if ( isDKIM && method.equals(CanonicalMethod.NOFWS)) {
			throw new DkimException(DkimError.LIBERROR,"You can not set nofws canonicalisation on a DKIM Signature");
		}

		if ( method.equals(CanonicalMethod.RELAXED) ) {
			sigCtag = "relaxed/relaxed";
		} else if ( method.equals(CanonicalMethod.NOFWS)) {
			sigCtag = "nofws";
		} else {
			if ( isDKIM )
				sigCtag = "simple/simple";
			else
				sigCtag = "simple";
		}
	}
	
	/**
	 * Generate the signature from the current tags and return the DKIM signature string.
	 * This function is intended to be used during Signing, for Verification, getDkimSig()
	 * is more appropriate.
	 * 
	 * @return DKIM-Signature.
	 */
	public String genDkimSig() {
		updateSigfromTags();
		return dkimSig;
	}
	
	/**
	 * Get the currently stored signature string. This is safer than genDkimSig() for verification
	 * purposes. Particularly if Leniency is enabled, as we may set tags, that the signer did not.
	 *
	 * @return DKIM-Signature.
	 */
	public String getDkimSig() {
		return dkimSig;
	}
	
	
	/**
	 * Reset the DKIM Signature tags to their defaults. Warning, this will also
	 * set mandatory tags to the null string. If you create an instance of this
	 * class and want to verify a signature is valid, then resetting the tags will
	 * detect if the signature is missing mandatory flags and breaking the RFC.
	 * If you wish to create a new message and/or be lenient on malformed tags (ie
	 * assume default values when mandatory flags are missing), then use the 
	 * resetDefaultTags() method instead.
	 */
	public void resetTags() {
		sigVtag = sigAtag = sigBtag = sigBHtag = "\0";
		sigDtag = sigHtag = sigItag = sigStag = "\0";
		sigStag = sigTtag = sigXtag = sigZtag = "\0";
		sigLtag = -1;
		if ( isDKIM ) {
			sigCtag = "simple/simple";
			sigQtag = "dns/txt";
		} else {
			sigCtag = "\0";
			sigQtag = "dns";
			sigAtag = "rsa-sha1";
		}
	}
	
	/**
	 * Reset the DKIM signature tags to their defaults (include mandatory tags where
	 * recommendations/defaults are made in the RFC). This is a little more lenient
	 * on bad signatures, but is mostly intended to be used when creating a signature
	 * rather than verifying one.  
	 */
	public void resetDefaultTags() {
		sigBtag = sigBHtag = "\0";
		sigDtag = sigHtag = sigItag = sigStag = "\0";
		sigStag = sigTtag = sigXtag = sigZtag = "\0";
		sigVtag = "1";	
		sigLtag = -1;	
		if ( isDKIM) {
			sigAtag = "rsa-sha256";
			sigCtag = "simple/simple";
			sigQtag = "dns/txt";
		} else {
			sigAtag = "rsa-sha1";
			sigCtag = "simple";
			sigQtag = "dns";
		}
	}
	
}
