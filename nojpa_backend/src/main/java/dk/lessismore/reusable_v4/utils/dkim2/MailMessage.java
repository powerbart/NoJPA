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
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * This simple class reads in a raw email message streams and stores the headers and
 * body in separate ByteArrayOutputStream variables for processing by the other DKIM
 * objects.
 * 
 * @author Mark Boddington &lt;dk_NO_im@_SP_bad_AM_penguin.co.uk&gt;
 *         <br>http://www.badpenguin.co.uk
 */
public class MailMessage {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MailMessage.class);

	private ByteArrayOutputStream headerStream = null;
	private ByteArrayOutputStream bodyStream = null;
	
	private int dkimHeaders;
	private int domkeyHeaders;
	
	/**
	 * Default constructor. Returns an empty MailMessage object.
	 */
	public MailMessage() {
		dkimHeaders = 0;
		domkeyHeaders = 0;
		headerStream = new ByteArrayOutputStream();
		bodyStream = new ByteArrayOutputStream();
	}
	
	/**
	 * This method performs most of the input processing of the raw mail
	 * messages. We initialise the ByteArrayOutputStreams and parse the message
	 * provided in the InputStream. Once parsed, the headers and body data can be
	 * retrieved by calling the appropriate methods.
	 *  
	 * @param msg - The raw email message as an InputStream
	 */
	public void processMail(InputStream msg) {

		Scanner mail =  new Scanner(msg);
		mail.useDelimiter(Pattern.compile("[\r\n]"));
		
		try {
			while ( mail.hasNextLine()) {
				String line = mail.nextLine();
				if ( line.isEmpty() ) {
					break;
				}
				
				if ( line.toLowerCase().startsWith("domainkey-dignature:"))
					domkeyHeaders++;
				if ( line.toLowerCase().startsWith("dkim-signature"))
					dkimHeaders++;
				
				line += "\r\n";
				headerStream.write(line.getBytes());
			}
		} catch (IOException e) {
			log.error("Encountered IO Error while buffering headers", e);
		}
		
		try {
			while ( mail.hasNextLine()) {
				String line = mail.nextLine();
				line += "\r\n";
				bodyStream.write(line.getBytes());
			}
		} catch (IOException e) {
			log.error("Encountered IO Error while buffering message body", e);
		}

	}
	
	/**
	 * Return the headers which were stored during the last processMail() invocation.
	 * @return The header stream 
	 */
	public ByteArrayOutputStream getHeaders() {
		return headerStream;
	}
	
	/**
	 * Return the body data which was stored during the last processMail() invocation.
	 * @return The body data stream 
	 */
	public ByteArrayOutputStream getBody() {
		return bodyStream;
	}
	
	/**
	 * Return the number of DKIM Signatures found in this mail message
	 * @return number of signatures
	 */
	public int dkimHeaderCount() {
		return dkimHeaders;
	}
	
	/**
	 * Return the number of DomainKey Signatures found in this mail message
	 * @return number of signatures
	 */
	public int domkeyHeaderCount() {
		return domkeyHeaders;
	}

}
