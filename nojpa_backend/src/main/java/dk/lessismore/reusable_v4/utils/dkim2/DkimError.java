/**
 * Java implementation of DKIM/DomainKeys. 
 * Copyright (c) 2008 Mark Boddington (www.badpenguin.co.uk)
 * 
 * This program is licensed under the terms of the GNU GPL version 2.0.
 * The DKIM specification is documented in RFC 4871
 * See: http://www.ietf.org/rfc/rfc4871.txt
 */
package dk.lessismore.reusable_v4.utils.dkim2;

/**
 * This class enumerates the possible DKIM and DomainKey errors specified in their
 * respective RFC's. The JavaDKIM library should throw a DkimException with the
 * appropriate DkimError and ErrorType when ever something is not quite right.
 * 
 * @author Mark Boddington &lt;dk_NO_im@_SP_bad_AM_penguin.co.uk&gt;
 *         <br>http://www.badpenguin.co.uk
 */
public enum DkimError {
	
	/**
	 * DomainKey status - The signature was verified at the time of testing
	 */
	good ("good", ErrorType.PASS, "The signature was verified at the time of testing"),
	/**
	 * DomainKey status - The signature failed verification
	 */
	bad ("bad", ErrorType.PERMFAIL, "The signature failed verification"),
	/**
	 * DomainKey status - The public key query failed as the key does not exist
	 */
	nokey ("no key", ErrorType.PERMFAIL, "The public key query failed as the key does not exist"),
	/**
	 * DomainKey status - The public key query failed as the key has been revoked
	 */
	revoked ("revoked", ErrorType.PERMFAIL, "The public key query failed as the key has been revoked"),
	/**
	 * DomainKey status - The signature or the public key contains unexpected data
	 */
	badformat ("bad format", ErrorType.PERMFAIL, "The signature or the public key contains unexpected data"),
	/**
	 * DomainKey status - The sending domain has indicated it does not participate in DomainKeys
	 */
	participant ("non-participant", ErrorType.PERMFAIL, "The sending domain has indicated it does not participate in DomainKeys"),
	
	
	/**
	 * Generic Temporary Failure 
	 */
	TEMPFAIL ("TEMPFAIL", ErrorType.TEMPFAIL, "Temporary Failure encountered. Message may validate later"),	
	/**
	 * Generic Permenant Failure encountered. 
	 */
	PERMFAIL ("PERMFAIL", ErrorType.PERMFAIL, "Permenant Failure encountered. Message will never validate"),
	/**
	 * DKIM status - PERMFAIL (Signature Syntax Error)
	 */
	SIGSYNTAX ("PERMFAIL - SIGSYNTAX", ErrorType.PERMFAIL, "(Signature Syntax Error)"),
	/**
	 * DKIM status - PERMFAIL (Incompatible Version)
	 */
	SIGVERSION ("PERMFAIL - SIGVERSION", ErrorType.PERMFAIL, "(Incompatible Version)"),
	/**
	 * DKIM status - PERMFAIL (Signature Missing Required Tag)
	 */
	SIGREQTAG ("PERMFAIL - SIGREQTAG", ErrorType.PERMFAIL, "(Signature Missing Required Tag)"),
	/**
	 * DKIM status - PERMFAIL (Domain Mismatch)
	 */
	SIGDOMAIN ("PERMFAIL - SIGDOMAIN", ErrorType.PERMFAIL, "(Domain Mismatch)"),
	/**
	 * DKIM status - PERMFAIL (From Header Not Signed)
	 */
	SIGFROM ("PERMFAIL - SIGFROM", ErrorType.PERMFAIL, "(From Header Not Signed)"),
	/**
	 * DKIM status - PERMFAIL (Signature Expired)
	 */
	SIGEXPIRED ("PERMFAIL - SIGEXPIRED", ErrorType.PERMFAIL, "(Signature Expired)"),
	/**
	 * DKIM status - PERMFAIL (Unacceptable Signature Header)
	 */
	SIGFAIL ("PERMFAIL - SIGFAIL", ErrorType.PERMFAIL, "(Unacceptable Signature Header)"),
	/**
	 * DKIM status - TEMPFAIL (Key Unavailable)
	 */
	KEYUNVAIL ("TEMPFAIL - KEYUNAVAIL", ErrorType.TEMPFAIL, "(Key Unavailable)"),
	/**
	 * DKIM status - PERMFAIL (No Key For Signature)
	 */
	NOKEY ("PERMFAIL - NOKEY", ErrorType.PERMFAIL, "(No Key For Signature)"),
	/**
	 * DKIM status - PERMFAIL (Key Syntax Error)
	 */
	KEYSYNTAX ("PERMFAIL - KEYSYNTAX", ErrorType.PERMFAIL, "(Key Syntax Error)"),
	/**
	 * DKIM status - PERMFAIL (Inapplicable Key)
	 */
	KEYFAIL ("PERMFAIL - KEYFAIL", ErrorType.PERMFAIL, "(Inapplicable Key)"),
	/**
	 * DKIM status - PERMFAIL (Inappropriate Hash Algorithm)
	 */
	KEYHASH ("PERMFAIL - KEYHASH", ErrorType.PERMFAIL, "(Inappropriate Hash Algorithm)"),
	/**
	 * DKIM status - PERMFAIL (Key Revoked)
	 */
	KEYREVOKED ("PERMFAIL - KEYREVOKED", ErrorType.PERMFAIL, "(Key Revoked)"),
	/**
	 * DKIM status - PERMFAIL (Inappropriate Key Algorithm)
	 */
	KEYALG ("PERMFAIL - KEYALG", ErrorType.PERMFAIL, "(Inappropriate Key Algorithm)"),
	/**
	 * DKIM status - PERMFAIL (Body Hash Did Not Verify)
	 */
	BODYHASH ("PERMFAIL - BODYHASH", ErrorType.PERMFAIL, "(Body Hash Did Not Verify)"),
	/**
	 * DKIM status - PERMFAIL (Signature Did Not Verify)
	 */
	SIGVERIFY ("PERMFAIL - SIGVERIFY", ErrorType.PERMFAIL, "(Signature Did Not Verify)"),
	/**
	 * DKIM status - PERMFAIL (Unsigned Content)
	 */
	CONTENT ("PERMFAIL - CONTENT", ErrorType.PERMFAIL, "(Unsigned Content)"), 
	
	/**
	 * This email has no DomainKey-Signature nand/nor DKIM header.
	 * Not strictly an error, it just means the email is not signed.
	 */
	NOSIG ("NOSIG", ErrorType.NOSIG, "This email has no DomainKey-Signature / DKIM header"),
	
	/**
	 * Library Error - An internal error has occurred
	 */
	LIBERROR ("LIBERROR", ErrorType.LIBERROR, "An internal error has occurred");
	
	private String status;
	private String description;
	public ErrorType errorType;
	
	/**
	 * Construct a new enumeration of DkimError
	 * 
	 * @param arg1 - The RFC error
	 * @param arg2 - The Error Type
	 * @param arg3 - The Description of the error.
	 */
	private DkimError(String arg1, ErrorType arg2, String arg3) {
		status = arg1;
		errorType = arg2;
		description = arg3;
	}
	
	/**
	 * Get the RFC error status
	 * @return The RFC error
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * Get the detailed description
	 * @return The Description
	 */
	public String getDescription() {
		return description;
	}

}
