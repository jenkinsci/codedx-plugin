package com.secdec.codedx.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;

public interface ExtraCertManager {

	/**
	 * Add a certificate that will be accepted until some event (as determined
	 * by the implementation of this interface) occurs, causing it to be
	 * "forgotten".
	 * 
	 * @param cert
	 */
	void addTemporaryCert(Certificate cert) throws IOException, GeneralSecurityException;

	/**
	 * Add a certificate that will be accepted until this manager is "purged".
	 * Certificates added in this way will generally be written to disk, and
	 * will be available upon restarting the program.
	 * 
	 * @param cert
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	void addPermanentCert(Certificate cert) throws IOException, GeneralSecurityException;

	/**
	 * Remove all certificates that have been added via
	 * {@link #addTemporaryCert(Certificate)}.
	 */
	void purgeTemporaryCerts() throws IOException, GeneralSecurityException;

	/**
	 * Remove all certificates that have been added via
	 * {@link #addPermanentCert(Certificate)}.
	 */
	void purgePermanentCerts() throws IOException, GeneralSecurityException;

	/**
	 * Remove all certificates that have been added either by
	 * {@link #addTemporaryCert(Certificate)} or
	 * {@link #addPermanentCert(Certificate)}.
	 */
	void purgeAllCerts() throws IOException, GeneralSecurityException;

	/**
	 * Return a representation of this manager as a KeyStore instance.
	 * @return A new KeyStore that represents the contents of this certificate
	 *         manager
	 */
	KeyStore asKeyStore() throws GeneralSecurityException;
}
