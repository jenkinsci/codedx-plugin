package com.secdec.codedx.security;

import com.secdec.codedx.util.HashUtil;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


public class FingerprintStrategy implements InvalidCertificateStrategy {

    private String fingerprint;

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
                String certFingerprint = HashUtil.toHexString(HashUtil.getSHA1(cert.getEncoded()), ":");
                if (certFingerprint.toUpperCase().equals(fingerprint.toUpperCase())) {
                    return CertificateAcceptance.ACCEPT_PERMANENTLY;
                }
            } catch (CertificateEncodingException exception) {
                exception.printStackTrace();
            }
        }
        return CertificateAcceptance.REJECT;
	}
}
