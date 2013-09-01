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
 * This is a class of exceptions which are thrown by the JavaDKIM library when
 * things go wrong. These Exceptions always include a DkimError, which includes a
 * short status string, an ErrorType (NOSIG, PASS, PERMFAIL, TEMPFAIL, or LIBERROR), 
 * and a description.
 * <br>
 * You will probably want to decide what to do with the exception based on the ErrorType.
 * 
 * @author Mark Boddington &lt;dk_NO_im@_SP_bad_AM_penguin.co.uk&gt;
 *         <br>http://www.badpenguin.co.uk
 */
public class DkimException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String message = null;
	private DkimError error = null;
	private Throwable cause = null;
	
	/**
	 * Create a DKIM Exception by specifying only the DkimError. The description
	 * of the DkimError will be returned to calls to getMessage()
	 * 
	 * @param arg0 - DkimError
	 */
	public DkimException(DkimError arg0) {
		error = arg0;
	}
	
	/**
	 * Create a DKIM Exception by specifying the DkimError type and a descriptive
	 * message of the problem.
	 * @param arg0 - DkimError
	 * @param arg1 - Description of the error
	 */
	public DkimException(DkimError arg0, String arg1) {
		error = arg0;
		message = arg1;
	}
	
	/**
	 * Create a DKIM Exception by specifying the DkimError type and a Throwable
	 * cause. This should be used when the code itself caught an Exception.
	 * @param arg0 - DkimError
	 * @param e - Throwable cause
	 */
	public DkimException(DkimError arg0, Throwable e) {
		error = arg0;
		cause = e;
	}
	
	/**
	 * Create a DKIM Exception by specifying the DkimError type, a descriptive
	 * message of the problem, and a Throwable cause. This should be used when the 
	 * code itself caught an Exception.
	 * @param arg0 - DkimError
	 * @param arg1 - Description of the error
	 * @param e - Throwable cause
	 */
	public DkimException(DkimError arg0, String arg1, Throwable e) {
		error = arg0;
		message = arg1;
		cause = e;
	}
	
	
	/**
	 * Return the error description provided when this exception was thrown, or the
	 * default description from DkimError.getDescription(), if no description was
	 * specified.
	 * @return The error message
	 */
	public String getMessage() {
		if ( message == null ) {
			return error.getStatus() + ": " + error.getDescription();
		} else {
			return error.getStatus() + ": " + message;
		}
	}
	
	/**
	 * Return the root cause of this Exception, if there was one.
	 * @return Throwable cause
	 */
	public Throwable getCause() {
		return cause;
	}
	
	/**
	 * Get the DkimError type for this exception.
	 * @return - Dkim Error
	 */
	public DkimError getError() {
		return error;
	}
	
	/**
	 * Return the ErrorType of this Exception
	 * @return ErrorType
	 */
	public ErrorType getErrorType() {
		return error.errorType;
	}


}
