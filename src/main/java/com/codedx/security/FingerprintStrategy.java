/*
 * © 2023 Synopsys, Inc. All rights reserved worldwide.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.codedx.security;

import com.codedx.util.HashUtil;

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
