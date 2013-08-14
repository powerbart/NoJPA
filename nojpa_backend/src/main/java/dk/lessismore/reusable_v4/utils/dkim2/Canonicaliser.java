/**
 * Java implementation of DKIM/DomainKeys. 
 * Copyright (c) 2008 Mark Boddington (www.badpenguin.co.uk)
 * 
 * This program is licensed under the terms of the GNU GPL version 2.0.
 * The DKIM specification is documented in RFC 4871
 * See: http://www.ietf.org/rfc/rfc4871.txt 
 */
package dk.lessismore.reusable_v4.utils.dkim2;

import java.io.ByteArrayOutputStream;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The Canonicaliser class is responsible for preparing a message for signing or 
 * verification. It understands the DKIM canonical methods simple and relaxed, as 
 * well as the DomainKey methods simple and nofws.
 * 
 * @author Mark Boddington &lt;dk_NO_im@_SP_bad_AM_penguin.co.uk&gt;
 *         <br>http://www.badpenguin.co.uk
 */
public class Canonicaliser {
	
	// The three canonical methods used in this canonicaliser
	private static final CanonicalMethod SIMPLE = CanonicalMethod.SIMPLE;
	private static final CanonicalMethod RELAXED = CanonicalMethod.RELAXED;
	private static final CanonicalMethod NOFWS = CanonicalMethod.NOFWS;
	
	// The headers which the RFC recommends we sign
	private static String[] RFCHEADERS = {
		"From", "Sender", "Reply-To", "Subject", "Date", "Message-ID", "To", "Cc",
		"MIME-Version", "Content-Type", "Content-Transfer-Encoding", "Content-ID", 
		"Content-Description", "Resent-Date", "Resent-From", "Resent-Sender", 
		"Resent-To", "Resent-Cc", "Resent-Message-ID", "In-Reply-To", "References",
		"List-Id", "List-Help", "List-Unsubscribe", "List-Subscribe", "List-Post",
		"List-Owner", "List-Archive"
	};
	
	// Is the signature preference for DKIM?
	private boolean useDKIM = false;
	
	// Should we look for the other header if the preferred isn't present?
	private boolean fallBack = false;
	
	// Are we signing?
	private boolean isSigning = false;
	
	// This stack holds all headers found in the current message
	private Stack<String> headerStack = null;
	
	// Which headers should we sign?
	private String signHeaders = null;
	
	// Two Stacks for holding multiple DomainKey and DKIM Signatures
	private Stack<String> dkimSigs = null;
	private Stack<String> domKeySigs = null;
	
	/**
	 * Create a new canonicaliser instance, which will prefer to use the Signature
	 * type specified in sigPref (either "DomainKey" or "DKIM"). If you initialise
	 * this instance for signing, then we will create the appropriate header based
	 * on this preference. If this instance is initialised for Verification, then
	 * we will attempt to verify the header of this preference, if both are present.
	 * 
	 * @param sigPref - Signature preference, DKIM or DomainKey
	 */
	public Canonicaliser(String sigPref) {
		if ( sigPref.equalsIgnoreCase("DKIM")) {
			useDKIM = true;
		} else if ( sigPref.equalsIgnoreCase("DomainKey")) {
			useDKIM = false;
		} else {
			//invalid option, preferring DKIM
			useDKIM = true;
		}
		fallBack = false;
	}
	

	/**
	 * Create a new canonicaliser instance. This instance will prefer to use the
	 * DKIM signature. If you initialise this instance for signing, then we will 
	 * create a DKIM-Signature. If you initialise for Verification, then we will
	 * chose a DKIM-Signature over a DomainKey-Signature, should both be present.
	 */
	public Canonicaliser() {
		useDKIM = true;
		fallBack = false;
	}
	
	/**
	 * Return a colon separated list of headers, which were found in this message,
	 * and are recommended for signing by the DKIM RFC. The string returned is in an
	 * appropriate format for use in the DkimSignature.setHtag(String) method.
	 * <br>
	 * We will throw a DkimException if you attempt to call this method, before you
	 * have called initHeaders()
	 *   
	 * @return Colon separated header list
	 * @throws DkimException
	 */
	public String getRecommendedHeaders() throws DkimException {
		
		// If the stack is empty, then we haven't been initialised. Throw an error
		if (headerStack == null ) {
			throw new DkimException(DkimError.LIBERROR, "You must call initHeaders first!");
		}
		
		String headers = RFCHEADERS[0];
		
		for ( int i = 1 ; i < RFCHEADERS.length ; i++) {
			if (headerStack.contains(RFCHEADERS[i])) {
				headers += ":" + RFCHEADERS[i];
			}
		}
		
		return headers.toLowerCase();
	}

	/**
	 * Take a line of input and canonicalise it in accordance with the method
	 * specified. Returns the canonicalised version of this line.
	 * 
	 * @param line - The line to canonicalise
	 * @param method - The method to use (simple|relaxed|nofws)
	 * @return The canonicalised input
	 */
	protected String processLine(String line, CanonicalMethod method) {
		
		if ( method.equals(NOFWS) ) {
			line = line.replaceAll("[\t\r\n ]", "");
			line += "\r\n";
		} else if ( method.equals(RELAXED) ) {
			line = line.replaceAll("[\r\n\t ]+", " ");
			line = line.replaceAll("(?m)[\t\r\n ]+$", "");
			line += "\r\n";
		} else if (method.equals(SIMPLE) ) {
			line += "\r\n";
		} 
		return line;
	}
	
	/**
	 * Read the body from the given byte stream and process it with the specified
	 * canonicalisation method. If the length argument is greater than -1, then we
	 * will truncate the body data to the given length. this method returns the
	 * body data in a String, ready to be signed or verified.
	 * 
	 * @param bodyStream - The body data to be processed
	 * @param length - The length at which to truncate the body, or -1
	 * @param method - The CanonicalMethod to use
	 * @return A string containing the canonicalised body
	 * @throws DkimException
	 */
	public String processBody(ByteArrayOutputStream bodyStream, long length, 
			CanonicalMethod method) throws DkimException {

		String mailBody = "";
		
		// if the DKIM L tag was 0, then return just CRLF
		if ( length == 0 ) {
			return "";
		}
		
		Scanner mail =  new Scanner(bodyStream.toString());
		mail.useDelimiter(Pattern.compile("[\r\n]"));
		
		while (mail.hasNextLine()) {
			String line = mail.nextLine();
			mailBody += processLine(line,method);
		}
		
		mailBody = mailBody.replaceFirst("[\r\n]*$", "\r\n");
		
		if ( length > 0 ) {
			
			String truncated = "";
			byte[] body = mailBody.getBytes();
			
			if ( length > body.length) {
				throw new DkimException(DkimError.SIGFAIL,"The (L)ength tag is larger than the messages actual length");
			}
			
			for ( int c = 0 ; c<length ; c++) {
				truncated += (char) body[c];
			}
			return truncated;
		} else {
			return mailBody;
		}
	}
	

	/**
	 * Process the headers provided during initialisation. We will read the Canonicalisation
	 * method, as well as other data from the provided DkimSignature object. This method 
	 * returns the headers in a String, ready to be signed or verified.
	 * 
	 * @param dkimSig - The DkimSignature object which relates to these headers
	 * @return The processed headers, ready for signing/verifying
	 * @throws DkimException
	 */
	public String processHeaders(DkimSignature dkimSig) throws DkimException {
	
		// We don't want to modify the original stack, so clone it
		Stack<String> myStack = (Stack<String>) headerStack.clone();
		
		// Clear the signHeaders
		signHeaders = "";

		// If the stack is empty, then we haven't been initialised. Throw an error
		if (headerStack == null || myStack == null ) {
			throw new DkimException(DkimError.LIBERROR,"You must call initHeaders first!");
		}
		
		// find out what's what from the DkimSignature object.
		String sigHtag = dkimSig.getHtag();
		CanonicalMethod method = dkimSig.getHeaderMethod();
		boolean isDKIM = dkimSig.isDKIM();
		
		// If we are processing DKIM and the H tag is empty, throw an error.
		if ( isDKIM ) {		
			if ( sigHtag == null || sigHtag.isEmpty() ) {
				throw new DkimException(DkimError.SIGREQTAG, "The madatory H tag appears to be missing from the DKIM-Signature");
			}	
		} 
		
		if ( sigHtag != "\0") {
			
			// If we're DKIM, add the dkim-signature to the end of the headers.
			if ( isDKIM ) {
				sigHtag += ":DKIM-Signature";
			}
			
			String[] headers = sigHtag.split("\\s*:\\s*");
			int headerCount = headers.length;
			String[] values = new String[headerCount];
			
			while ( ! myStack.isEmpty() ) {
				String header = myStack.pop();
				for ( int index = 0 ; index < headerCount ; index++ ) {
					if ( header.trim().equalsIgnoreCase(headers[index].trim()) ) {
						headers[index] = header;
						if ( values[index] == null ) {
							if ( headers[index].equalsIgnoreCase("dkim-signature") && ( ! isSigning )) {
								
								values[index] = myStack.pop();
								Pattern pattern = Pattern.compile(".*?b=(.*?)[\\s;].*");
							    Matcher matcher = pattern.matcher(values[index]);
							    String bt = null;
								if ( matcher.find() ) {
									bt = matcher.group(1);
									if ( dkimSig.getBtag().contains(bt) ) {
										values[index] = values[index].replaceFirst("b=[A-Za-z0-9+/=\r\n\t ]+", "b=");
									} else {
										// this is not the signature we're looking for
										values[index] = null;
									}
								}
							} else {
								//values[index] =  processLine(myStack.pop(),method);
								values[index] = myStack.pop();
							}
							break;
						}
					}
				}
			}
			
			if ( isSigning ) {
				// Get our DKIM-Sig, and skip over the Header, so we have the value.
				values[headerCount-1] = processLine(dkimSig.genDkimSig().substring(15) ,method);
			}
			
			for (int index = 0 ; index < headers.length ; index++ ) {
				if ( method.equals(RELAXED) ){
					signHeaders += processLine(headers[index].toLowerCase().trim() + ":" + values[index].trim(),method);
				} else if ( method.equals(NOFWS)){
					signHeaders += processLine(headers[index].trim() + ":" + values[index].trim(),method);
				} else {
					signHeaders += processLine(headers[index] + ":" + values[index],method);
				}
			}
				
		} else {		
			// We must be a DomainKey, with no headers, if anything adds headers, we wont verify :-(
			while ( ! myStack.isEmpty() ) {
				String header = myStack.pop();
				String value = myStack.pop();
				if ( header.equalsIgnoreCase("DomainKey-Signature"))
					continue;
				signHeaders = processLine(header + ":" + value, method) + signHeaders;
			}	
		}
			
		if ( isDKIM ) {
			// remove trailing <CRLF> from the DKIM-Signature header.
			signHeaders = signHeaders.replaceAll("(?m)[\r\n]+$", "");
		}
		
		return signHeaders;
	}
				
	/**
	 * Initialise this Canonicaliser for Verification. We will read in the headers
	 * from the message, and they will be placed onto a stack for later processing
	 * with processHeaders(). This method will return either the DKIM or DomainKey
	 * signature based on the preference set during initialisation. If the fallback
	 * option is set to true, then we will return the other signature, should the
	 * preferred signature be unavailable.
	 * 
	 * @param headerStream - The message headers to be read
	 * @param fallback - Should we fall back to the other DomainKey header?
	 * @return The DKIM-Signature or DomainKey-Signature
	 * @throws DkimException
	 */
	public String initVerify(ByteArrayOutputStream headerStream, boolean fallback) throws DkimException{
		
		headerStack = new Stack<String>();
		signHeaders = null;
		fallBack = fallback;
		isSigning = false;

		return init(headerStream,false);
	}
	
	/**
	 * Initialise this Canonicaliser for Verification. We will read in the headers
	 * from the message, and they will be placed onto a stack for later processing
	 * with processHeaders(). This method will store all DKIM and DomainKey Signatures
	 * inside Stacks, which can then be accessed via....
	 *
	 *  
	 * @param headerStream
	 * @throws DkimException
	 */
	public void initVerify(ByteArrayOutputStream headerStream) throws DkimException{
		
		headerStack = new Stack<String>();
		signHeaders = null;
		fallBack = true;
		isSigning = false;

		init(headerStream,true);
	}
	
	/**
	 * Initialise this canonicaliser for use in signing. A flag will be set to indicate
	 * the that the DKIM-signature is being created and not to be expected in the mail
	 * headers.
	 * 
	 * @param headerStream - The message headers to be read.
	 * @throws DkimException
	 */
	public void initSign(ByteArrayOutputStream headerStream) throws DkimException {

		headerStack = new Stack<String>();
		signHeaders = null;
		isSigning = true;
		
		init(headerStream,false);
	}
	
	/**
	 * If the Canonicaliser was initialised for verification with initVerify(headerStream), then
	 * all DKIM signatures will be available in a Stack. The Stack is ordered in a LIFO manner,
	 * so the last DKIM Signature found will be at index 0.<br>
	 * Please note that the returned Stack may be empty.<br>
	 * We will throw a DkimException if the Stack is uninitialised, the most likely cause of which
	 * is that initVerify(headerStream, fallback) was called, which returns a single Signature itself,
	 * and does not store multiple signatures.
	 * 
	 * @return Stack<String> of DKIM Signatures
	 * @throws DkimException
	 */
	public Stack<String> getDkimHeaders() throws DkimException {
		if ( dkimSigs == null)
			throw new DkimException(DkimError.LIBERROR, "Multiple headers must be configured with initVerify()");
		return dkimSigs;
	}
	
	/**
	 * If the Canonicaliser was initialised for verification with initVerify(headerStream), then
	 * all DomainKey signatures will be available in a Stack. The Stack is ordered in a LIFO 
	 * manner, so the last DomainKey Signature found will be at index 0.<br>
	 * Please note that the returned Stack may be empty.<br>
	 * We will throw a DkimException if the Stack is uninitialised, the most likely cause of which
	 * is that initVerify(headerStream, fallback) was called, which returns a single Signature itself,
	 * and does not store multiple signatures.
	 * 
	 * @return Stack<String> of DomainKey Signatures
	 * @throws DkimException
	 */
	public Stack<String> getDomKeyHeaders() throws DkimException {
		if ( domKeySigs == null)
			throw new DkimException(DkimError.LIBERROR, "Multiple headers must be configured with initVerify()");
		return domKeySigs;
	}
	
	/**
	 * This function performs most of the work for the public initVerify() and
	 * initSign() methods. It takes in the message headers and pushes them onto a
	 * stack. We return a DKIM or DomainKey signature based on the signature
	 * preference and the fall back option.
	 * <br>
	 * If the multiple option is set, then we will store all Signatures we find in a
	 * Stack, which can be read later. This is used when the Verifier wants to try more
	 * than one Signature, in the case where there are many.
	 * 
	 * @param headerStream - The messages headers.
	 * @param multiple - Should we provide all Signatures?
	 * @return The DKIM or DomainKey header
	 * @throws DkimException
	 */
	private String init(ByteArrayOutputStream headerStream, boolean multiple) throws DkimException {
		
		String domKeySig = null;
		String dkimSig = null;
		
		if ( multiple ) {
			dkimSigs = new Stack<String>();
			domKeySigs = new Stack<String>();
		}
		
		Scanner mail =  new Scanner(headerStream.toString());
		mail.useDelimiter(Pattern.compile("[\r\n]"));
		
		while (mail.hasNextLine()) {
			
			String line = mail.nextLine();
			while ( mail.hasNextLine()) {
				
				if ( mail.hasNext(Pattern.compile("(?m)^\\s+.*")) ) {
					line += "\r\n" + mail.nextLine();
				} else {
					break;
				}
			}

			int colon = line.indexOf(':');			
			if ( colon == -1 ) {
				// Broken message header :-(
				throw new DkimException(DkimError.PERMFAIL, "Broken Mail Header encountered, Message rejected. [" + line + "]");
			}
			
			headerStack.push(line.substring(colon+1) );
			headerStack.push(line.substring(0, colon) );
						
			if ( line.toLowerCase().startsWith("domainkey-signature")) {
				line = line.replaceAll("[\t\r\n ]+", " ");
				domKeySig = line.trim();
				if ( multiple ) 
					domKeySigs.push(domKeySig);
			} else if (line.toLowerCase().startsWith("dkim-signature") ) {
				line = line.replaceAll("[\r\n\t ]+", " ");
				dkimSig = line.trim();
				if ( multiple )
					dkimSigs.push(dkimSig);
			}	
		}
		
		if ( isSigning ) {
			return null;
		}
		
		if ( useDKIM && dkimSig != null) {
			return dkimSig;
		} else if ( !useDKIM && domKeySig != null) {
			return domKeySig;
		} else if ( fallBack && useDKIM && domKeySig != null ) {
			return domKeySig;
		} else if ( fallBack && !useDKIM && dkimSig != null ) {
			return dkimSig;
		} else {
			throw new DkimException(DkimError.NOSIG);
		}
		
	}
	
}
