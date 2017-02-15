package com.secdec.codedx.security;

import com.secdec.codedx.util.HashUtil;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;


public class FingerprintStrategy implements InvalidCertificateStrategy {

	private String fingerprint;
	private final static Logger logger = Logger.getLogger(FingerprintStrategy.class.getName());

	public FingerprintStrategy(String fingerprint) {
		this.fingerprint = fingerprint;
		if (fingerprint == null) {
			this.fingerprint = "";
		}
	}

	public CertificateAcceptance checkAcceptance(Certificate genericCert, CertificateException certError) {
		if (genericCert instanceof X509Certificate) {
			X509Certificate cert = (X509Certificate) genericCert;
			try {
				String certFingerprint = HashUtil.toHexString(HashUtil.getSHA1(cert.getEncoded()));
				logger.info("Certificate fingerprint:  " + certFingerprint.toUpperCase());
				logger.info("User-entered fingerprint: " + fingerprint.toUpperCase());
				if (certFingerprint.toUpperCase().equals(fingerprint.toUpperCase())) {
					return CertificateAcceptance.ACCEPT_PERMANENTLY;
				}
			} catch (CertificateEncodingException exception) {
				logger.warning("Problem reading certificate: " + exception);
				exception.printStackTrace();
			}
		} else {
			logger.warning("Certificate presented was not X509: " + genericCert);
		}
		return CertificateAcceptance.REJECT;
	}
}
