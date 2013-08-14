/**
 * Java implementation of DKIM/DomainKeys. 
 * Copyright (c) 2008 Mark Boddington (www.badpenguin.co.uk)
 * 
 * This program is licensed under the terms of the GNU GPL version 2.0.
 * The DKIM specification is documented in RFC 4871
 * See: http://www.ietf.org/rfc/rfc4871.txt
 */
package dk.lessismore.reusable_v4.utils.dkim2;

import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;


/**
 * The NSKeyStore can be used to retrieve keys from a Naming Service (Currently
 * only DNS) and store them in a hash table. Any subsequent requests for the same 
 * domain key record are returned directly from the hash table, thus reducing the
 * number of DNS queries performed during verification.
 * <br>
 * Each key is given an expiry time, and once the expiry time has passed, a new
 * name resolution will be made.
 * 
 * @author Mark Boddington &lt;dk_NO_im@_SP_bad_AM_penguin.co.uk&gt;
 *         <br>http://www.badpenguin.co.uk
 */
public class NSKeyStore {
	
	private static Hashtable<String,NSKey[]> keyMap = null;
	private DirContext dnsCtx = null;
	private int keyLimit;
	private int cacheTime; 

	/**
	 * Create a new KeyStore object. You must provide an IP address of you name server
	 * and the type of nameserver. Currently only DNS is supported by DKIM.
	 *
	 * @throws NamingException
	 */
	public NSKeyStore() throws NamingException {
		// Default Key Limit
		keyLimit = 3;
		
		// Default caching time (15 minutes);
		cacheTime = 900;
		
		keyMap = new Hashtable<String,NSKey[]>();
		Hashtable<String,String> env = new Hashtable<String,String>();	
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		//env.put("java.naming.provider.url", "dns://" + nameServer );
		dnsCtx = new InitialDirContext(env);

	}
	
	/**
	 * This method returns a NSKey object for the give domain key record. If the
	 * lookup has been performed before, then the NSKey will be returned from the
	 * hash table, otherwise a Name Service query will be initiated and the result
	 * will be stored and returned.
	 * 
	 * @param lookup - The domain key record to retrieve
	 * @return The NSKey for this request
	 * @throws DkimException - The NameService lookup has failed.
	 */
	public NSKey[] retrieveKeys(String lookup) throws DkimException {
		
		boolean updating = false;
		NSKey[] cached = null;
		
		if ( keyMap.containsKey(lookup)) {
			cached = keyMap.get(lookup);
			if ( cached[0].isExpired() ) {
				if ( cached[0].setUpdating(true )) {
					updating = true;
				} else {
					int retries = cached[0].getRetries();
					if ( retries > 5 ) {
						throw new DkimException(DkimError.TEMPFAIL, "Failed to perform NameService lookup");
					} 
					return cached;
				}
			} else {
				return cached;
			}
		} 

		Attributes attrs = null;

		try {
			attrs = dnsCtx.getAttributes(lookup, new String[] {"txt"});
		} catch (NamingException e) {
			if ( e.getMessage().contains("name not found") ) {
				throw new DkimException(DkimError.PERMFAIL,"No key for signature found at " + lookup, e);
			}
			if ( updating ) {
				int retries = cached[0].getRetries();
				//System.err.println("Key expired for: " + lookup + ", update failed. Retries: " + retries);
				if ( retries > 5 ) {
					throw new DkimException(DkimError.TEMPFAIL, "Failed to perform NameService lookup", e);
				} 
				return cached;
			}
			throw new DkimException(DkimError.TEMPFAIL, "Failed to perform NameService lookup", e);
		} finally {
			if ( updating ) {
				cached[0].setUpdating(false);
			}
		}

		String[] record = attrs.toString().split(",");

		// The first entry will start with "{txt=TXT:", so skip it ;-)
		record[0] = record[0].replace("{txt=TXT: ", "");

		// use a keyLimit to protect our selves from DOS.
		int keys = record.length;
		if ( keys > keyLimit ) {
			System.err.println("WARNING: Lookup \"" + lookup + "\" returned " + 
					keys + " records." + " Only storing " + keyLimit + " of them.");
			keys = keyLimit;
		}

		NSKey[] nsKey = new NSKey[keys];

		for (int i = 0; i< keys; i++) {
			nsKey[i] = new NSKey(record[i], cacheTime);
		}

		synchronized(keyMap) {
			if ( updating )
				keyMap.remove(lookup);
			if ( ! keyMap.contains(lookup))
				keyMap.put(lookup, nsKey);
		}
		
		return nsKey;	
	}
	
	/**
	 * The KeyStore limits the number of keys it stores, to prevent someone adding 1000
	 * key records and causing a DOS attack. The default limit is 3 keys per name.
	 * @return current key limit
	 */
	public int getKeyLimit() {
		return keyLimit;
	}
	
	/**
	 * The KeyStore limits the number of keys it stores, to prevent someone adding 1000
	 * key records and causing a DOS attack. The default limit is 3 keys per name.
	 * @param keyLimit Key Limit
	 */
	public void setKeyLimit(int keyLimit) {
		this.keyLimit = keyLimit;
	}
	
	/**
	 * Set the amount of time in seconds, that keys should be kept in cache. Once keys
	 * expire, they will need to be resolved again. The default cache time is 15
	 * minutes (900 seconds).
	 * 
	 * @param cacheTime
	 */
	public void setCacheTime(int cacheTime) {
		this.cacheTime = cacheTime;
	}
	
	/**
	 * Get the current cache time for the DKIM keys. The default is 15 minutes
	 * (900 seconds).
	 * 
	 * @return The number of seconds that records are cached.
	 */
	public int getCacheTime() {
		return cacheTime;
	}
	
}
