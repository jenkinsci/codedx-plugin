package com.secdec.codedx.security;

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
