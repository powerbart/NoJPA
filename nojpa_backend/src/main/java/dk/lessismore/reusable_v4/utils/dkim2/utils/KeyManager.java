/**
 * Java implementation of DKIM/DomainKeys. 
 * Copyright (c) 2008 Mark Boddington (www.badpenguin.co.uk)
 * 
 * This program is licensed under the terms of the GNu GPL version 2.0.
 * The DKIM specification is documented in RFC 4871
 * See: http://www.ietf.org/rfc/rfc4871.txt
 */
package dk.lessismore.reusable_v4.utils.dkim2.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
/**
 * This KeyManager is used by SignMail for accessing the Signing Key from a Java KeyStore
 * 
 * @author Mark Boddington &lt;dk_NO_im@_SP_bad_AM_penguin.co.uk&gt;
 *         <br>http://www.badpenguin.co.uk
 *
 */
public class KeyManager {
	
	KeyStore ks = null;
	String password = null;
	
	public KeyManager(FileInputStream store, String pass){
		
		password = pass;
		
		try {
			ks = KeyStore.getInstance("JKS");
			ks.load(store, pass.toCharArray());
		} catch (KeyStoreException e ) {
			e.printStackTrace();
		} catch( IOException e ) {
			System.err.println("Failed to open keystore.");
			e.printStackTrace();
		} catch (Exception e ) {
			e.printStackTrace();
		}
	}
	
	public PrivateKey getKey(String alias) {
		PrivateKey key = null;
		try {
			key = (PrivateKey) ks.getKey(alias, password.toCharArray());
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return key;
	}
	
	public PublicKey getCert(String alias) {
		PublicKey key = null;
		try {
			key = ks.getCertificate(alias).getPublicKey();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} 
		return key;
	}
	
	public Key getKey(String alias, String pass) {
		Key key = null;
		try {
			key = ks.getKey(alias, pass.toCharArray());
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return key;
	}

	
}
