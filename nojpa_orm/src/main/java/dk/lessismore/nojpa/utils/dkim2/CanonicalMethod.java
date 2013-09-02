/**
 * Java implementation of DKIM/DomainKeys. 
 * Copyright (c) 2008 Mark Boddington (www.badpenguin.co.uk)
 * 
 * This program is licensed under the terms of the GNU GPL version 2.0.
 * The DKIM specification is documented in RFC 4871
 * See: http://www.ietf.org/rfc/rfc4871.txt
 */
package dk.lessismore.nojpa.utils.dkim2;

/**
 * This class enumerates the three possible Canonicalisation methods, simple, nofws,
 * and relaxed.
 * 
 * @author Mark Boddington &lt;dk_NO_im@_SP_bad_AM_penguin.co.uk&gt;
 *         <br>http://www.badpenguin.co.uk
 */
public enum CanonicalMethod {


	/**
	 * The Simple Canonicalisation method
	 */
	SIMPLE ("The Simple Canonicalisation method", true, true),
	/**
	 * The No Folding White Space Canonicalisation method
	 */
	NOFWS ("The No folding Whitespace method (DomainKey only)", true, false),
	/**
	 * The Relaxed Canonicalisation method
	 */
	RELAXED ("The relaxed method (DKIM only)", false, true);
	
	private String description;
	private boolean domainkey;
	private boolean dkim;
	
	/**
	 * Construct a new enumeration of CanonicalMethod
	 * 
	 * @param arg1 - The Description
	 * @param arg2 - Valid DomainKey Canonicaliser?
	 * @param arg3 - Valid DKIM Canonicaliser?
	 */
	private CanonicalMethod(String arg1, boolean arg2, boolean arg3) {
		description = arg1;
		domainkey = arg2;
		dkim = arg3;
	}
	
	/**
	 * Return whether this CanonicalMethod is valid for DomainKey
	 * @return true or false
	 */
	public boolean validDomainKey() {
		return domainkey;
	}
	
	/**
	 * Return whether this CanonicalMethod is valid for DKIM
	 * @return true or false
	 */
	public boolean validDKIM() {
		return dkim;
	}
	
	/**
	 * Get the descriptive name for this canonical method.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
}
