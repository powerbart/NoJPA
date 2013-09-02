/**
 * 
 */
package dk.lessismore.nojpa.utils.dkim2;

/**
 * This class contains the error groups that will be thrown by the Library. 
 * All DkimExceptions will hold information about the type of error...
 * <p>
 *   NOSIG -- There was no Dkim or DomainKey Signature<br>
 *   TEMPFAIL -- Failed to verify Signature, but it may verify in future<br>
 *   PERMFAIL -- Siganture will never verify<br>
 *   LIBERROR -- The JavaDKIM library encountered an error<br>
 * </p>
 * 
 * @author Mark Boddington &lt;dk_NO_im@_SP_bad_AM_penguin.co.uk&gt;
 *         <br>http://www.badpenguin.co.uk
 */
public enum ErrorType {

	/**
	 * There was no DKIM or DomainKey Signature in the message
	 */
	NOSIG (0, "There was no Signature"),
	/**
	 * The message Signature passed validation ;-)
	 */
	PASS (1, "Message Siganture Verified"), 
	/**
	 * Temporary Failure encountered. Message may validate later
	 */
	TEMPFAIL (2, "Temporary Failure encountered. Message may validate later"),	
	/**
	 * Permenant Failure encountered. Message will never validate
	 */
	PERMFAIL (3, "Permenant Failure encountered. Message will never validate"),
	LIBERROR (10, "Java/Library Error");
	
	private int errorCode;
	private String description;
	
	private ErrorType(int errorCode, String desc) {
		this.errorCode = errorCode;
		this.description = desc;
	}
	
	/**
	 * Get the detailed description of this ErrorType
	 * @return description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Return the error code for this ErrorType
	 * 
	 * @return error code
	 */
	public int getErrorCode() {
		return errorCode;
	}
	
}
