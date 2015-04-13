package com.secdec.codedx.security;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class JenkinsSSLConnectionSocketFactoryFactory {

    public static SSLConnectionSocketFactory getFactory(String fingerprint, String host) throws GeneralSecurityException {
        // set up the certificate management
        ExtraCertManager certManager = new SingleCertManager("floopydoop");

        // get the default hostname verifier that gets used by the modified one
        // and the invalid cert dialog
        X509HostnameVerifier defaultHostnameVerifier = SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;

        // invalid cert strat that pops up a dialog asking the user if they want
        // to accept the cert
        FingerprintStrategy certificateStrategy = new FingerprintStrategy(fingerprint);

		/*
		 * Set up a composite trust manager that uses the default trust manager
		 * before delegating to the "reloadable" trust manager that allows users
		 * to accept invalid certificates.
		 */
        List<X509TrustManager> trustManagersForComposite = new LinkedList<X509TrustManager>();
        X509TrustManager systemTrustManager = getDefaultTrustManager();
        ReloadableX509TrustManager customTrustManager = new ReloadableX509TrustManager(certManager, certificateStrategy);
        trustManagersForComposite.add(systemTrustManager);
        trustManagersForComposite.add(customTrustManager);
        X509TrustManager trustManager = new CompositeX509TrustManager(trustManagersForComposite);

        // setup the SSLContext using the custom trust manager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] { trustManager }, null);

        // the actual hostname verifier that will be used with the socket
        // factory
        Set<String> allowedHosts = new HashSet<String>();
        allowedHosts.add(host);
        X509HostnameVerifier modifiedHostnameVerifier = new X509HostnameVerifierWithExceptions(defaultHostnameVerifier, allowedHosts);

        return new SSLConnectionSocketFactory(sslContext, modifiedHostnameVerifier);
    }

    private static X509TrustManager getDefaultTrustManager() throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory defaultFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        defaultFactory.init((KeyStore) null);

        TrustManager[] managers = defaultFactory.getTrustManagers();
        for (TrustManager mgr : managers) {
            if (mgr instanceof X509TrustManager) {
                return (X509TrustManager) mgr;
            }
        }

        return null;
    }
}
