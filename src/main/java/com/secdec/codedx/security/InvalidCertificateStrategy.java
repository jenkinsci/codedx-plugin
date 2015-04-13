package com.secdec.codedx.security;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public interface InvalidCertificateStrategy {
	/**
	 * Determine what to do with a certificate (reject, accept temporarily, or
	 * accept permanently)
	 * 
	 * @param cert A (probably invalid) certificate
	 * @param certError An exception (or null) that caused the certificate to be
	 *            considered invalid
	 * @return A CertificateAcceptance value that determines whether (and for
	 *         how long) the certificate should be considered valid.
	 */
	CertificateAcceptance checkAcceptance(Certificate cert, CertificateException certError);
}
