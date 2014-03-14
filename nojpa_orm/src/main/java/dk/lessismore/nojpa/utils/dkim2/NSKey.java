/**
 * Java implementation of DKIM/DomainKeys. 
 * Copyright (c) 2008 Mark Boddington (www.badpenguin.co.uk)
 * 
 * This program is licensed under the terms of the GNU GPL version 2.0.
 * The DKIM specification is documented in RFC 4871
 * See: http://www.ietf.org/rfc/rfc4871.txt
 */
package dk.lessismore.nojpa.utils.dkim2;

import java.security.PublicKey;
import java.util.Date;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64;
import sun.security.rsa.RSAPublicKeyImpl;


/**
 * This Object is used to store a public key and the associated meta data retrieved
 * from DNS (or maybe some other naming system in future). Typically these keys are
 * created and stored in a HashTable by the NSKeyStore object, which means we don't
 * have to make a DNS call every time the DKIM library parses a mail message.
 * 
 * @author Mark Boddington &lt;dk_NO_im@_SP_bad_AM_penguin.co.uk&gt;
 *         <br>http://www.badpenguin.co.uk
 */
public class NSKey {
	
	private PublicKey key = null;
	private String granularity = "*";
	private String hash = "sha1:sha256";
	private String notes = null;
	private String service = "*";
	private long expires;
	private String updating = "n";
	private int retries = 0;
	private DkimException dke;
	
	// flags
	private boolean testing = false;
	private boolean noSubdomains = false;
	
	/**
	 * Create a new NSKey object, from the Name Service text record provided in 
	 * the record string. This string should be a DNS txt record which adheres
	 * to the _domainkey DNS TXT record specification
	 * 
	 * @param record - The Name Services TXT record
	 * @param cacheTime - number of seconds to cache key for.
	 */
	public NSKey(String record, int cacheTime)  {
				
		Scanner keyScan = new Scanner(record);
		keyScan.useDelimiter("[\\;]");
		
		// Check and store exceptions. We will throw them when the key is requested.
		dke = null;

		expires = new Date().getTime() + ( cacheTime * 1000 );
		
		while ( keyScan.hasNext()) {
			String line = keyScan.next().replaceAll("[\t\" ]", "");
			String tag[] = line.trim().split("=",2);
			if ( tag[0].equals("g")) {
				granularity=tag[1];
			} else if (tag[0].equals("t")) {
				int i=0;
				if ( tag[1].contains("y") ) {
					testing = true;
					i++;
				} else if ( tag[1].contains("s")) {
					noSubdomains = true;
				} 
			} else if ( tag[0].equals("p")) {
				
				if ( tag[1].isEmpty() ) {
					dke = new DkimException(DkimError.KEYREVOKED,"The Key has been revoked.");
					return;
				}

				try{
					byte[] decoded = Base64.decodeBase64(tag[1]);
                    key = new RSAPublicKeyImpl( decoded );
					
				} catch (Exception e) {
					dke = new DkimException(DkimError.KEYSYNTAX,"The Key seems to be invalid",e);
					return;
				}
			} else if ( tag[0].equals("v")) {
				if ( ! tag[1].equals("DKIM1") ) {
					dke = new DkimException(DkimError.KEYFAIL,"Unsupported DKIM version in txt record");
					return;
				}
			} else if ( tag[0].equals("h")) {
				hash = tag[1];
			} else if ( tag[0].equals("k")) {
				if ( ! tag[1].equals("rsa")) {
					dke = new DkimException(DkimError.KEYALG,"Unsupported key type in txt record");
					return;
				}
			}
		}
		
		if ( key == null) {
			dke = new DkimException(DkimError.KEYFAIL,"No KEY found in txt record");
			return;
		}
		
	}
	
	/**
	 * Check if this key should be expired from the cache. 
	 * @return true or false
	 */
	public boolean isExpired() {
		if ( new Date().getTime() > expires )
			return true;
		else
			return false;
	}
	
	/**
	 * The NSKeyStore calls this method when it wants to update the key. We synchronise the update
	 * flag so that only one thread will attempt an update at once. Return true if the calling thread
	 * may perform an update, and false if not.
	 * <br/>
	 * If the update parameter is true, then the caller wants to perform an update. If it is false, then the
	 * caller is indicating that the update was unsuccessful.
	 * 
	 * @param update true or false
	 * @return boolean - allow update or not
	 */
	public boolean setUpdating(boolean update) {
		synchronized(updating) {
			if ( update == true ) {
				if (updating.equalsIgnoreCase("n")) {
					updating = "y";
					retries++;
					return true;
				}
			} else if ( update == false ) {
				updating = "n";
				return true;
			}
			return false;
		}
	}
	
	public int getRetries() {
		return retries;
	}
	
	/**
	 * Retrieve the public key from this NSKey object. The Public key can then be
	 * used for verification tasks. If the key has expired, then we will throw a
	 * 
	 * 
	 * @return The RSA Public Key
	 */
	public PublicKey getKey() throws DkimException {
		if ( dke != null) {
			throw dke;
		}
		return key;
	}
	
	/**
	 * Returns true if this NSKey record has the "y" flag set. Indicating that this
	 * domain is currently testing DKIM, Failed messages should be treated as though
	 * they had no signature.
	 * 
	 * @return True or False
	 */
	public boolean isTesting() {
		return testing;
	}
	
	/**
	 * Returns true if this NSKey record has the s flag set. The s flag indicates
	 * that if a DKIM-Signature has the i tag set, then it can not be a subdomain
	 * of the d tag. That is the right hand side of @ in both i and d tags must
	 * match exactly.
	 * 
	 * @return True or False
	 */
	public boolean noSubdomains() {
		return noSubdomains;
	}
	
	/**
	 * Retrieve the granularity value specified in the G tag for this key.
	 * @return granularity
	 */
	public String getGranularity() {
		return granularity;
	}
	
	/**
	 * Return the hash algorithms allowed for use with this key, as secified in the H tag.
	 * @return colon separated list of hash algorithms
	 */
	public String getHashAlgorithm() {
		return hash;
	}

}
