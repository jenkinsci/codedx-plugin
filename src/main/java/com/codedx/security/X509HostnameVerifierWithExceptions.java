/*
 * © 2024 Black Duck Software, Inc. All rights reserved worldwide.
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

import java.util.Set;

import javax.net.ssl.SSLException;

import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;

/**
 * X509HostnameVerifier implementation that delegates to another one, but will
 * allow a particular set of hosts through even if the delegate verifier fails.
 */
public class X509HostnameVerifierWithExceptions extends AbstractVerifier {

	private final X509HostnameVerifier delegate;
	private final Set<String> allowedExceptions;

	public X509HostnameVerifierWithExceptions(X509HostnameVerifier delegate, Set<String> allowedExceptions) {
		this.delegate = delegate;
		this.allowedExceptions = allowedExceptions;
	}


	public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
		try {
			delegate.verify(host, cns, subjectAlts);
		} catch (SSLException e) {
			// swallow the exception IFF the allowed hosts set contains the host
			if (!allowedExceptions.contains(host)) throw e;
		}
	}

}
