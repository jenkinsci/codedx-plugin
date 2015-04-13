package com.secdec.codedx.security;

/**
 * Enumeration to describe the possible outcomes of an
 * {@link InvalidCertificateStrategy} when presented with an invalid
 * certificate.
 */
public enum CertificateAcceptance {

	/**
	 * The invalid certificate should be rejected.
	 */
	REJECT,

	/**
	 * The invalid certificate should be accepted on a short-term basis, e.g.
	 * for the duration of the session, or until the current JVM stops. The
	 * actual interpretation is up to the corresponding {@link ExtraCertManager}
	 * .
	 */
	ACCEPT_TEMPORARILY,

	/**
	 * The invalid certificate should be accepted on a long-term basis, e.g. by
	 * adding the certificate to a custom KeyStore and persisting it to disk.
	 * The actual interpretation is up to the corresponding
	 * {@link ExtraCertManager}.
	 */
	ACCEPT_PERMANENTLY;

}
