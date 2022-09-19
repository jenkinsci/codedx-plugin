/*
 *
 * © 2022 Synopsys, Inc. All rights reserved worldwide.
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
 *
 */

package com.secdec.codedx.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;

/**
 * ExtraCertManager implementation that only allows a single accepted
 * certificate at once. Any time a certificate is added (be it temporarily or
 * permanently), any previous certificates will be forgotten. At any given time,
 * the {@link #asKeyStore()} method should return a KeyStore with 0 or 1
 * certificates registered.
 */
public class SingleCertManager implements ExtraCertManager {

	private char[] password;
	private KeyStore keyStore;

	public SingleCertManager(String password) {
		this.password = password.toCharArray();
	}


	public void addTemporaryCert(Certificate cert) {
	}

	public void addPermanentCert(Certificate cert) throws IOException, GeneralSecurityException {
		// create a keystore and put the cert in it
		keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null, password);
		keyStore.setCertificateEntry("default", cert);
	}

	public void purgeTemporaryCerts() {
	}

	public void purgePermanentCerts() {
	}

	public void purgeAllCerts() {
	}

	public KeyStore asKeyStore() throws GeneralSecurityException {
		return keyStore;
	}
}
