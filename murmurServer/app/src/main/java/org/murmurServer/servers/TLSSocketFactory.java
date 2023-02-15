package org.murmurServer.servers;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.security.KeyStore;

public class TLSSocketFactory {
    private final String certificatePath;
    private final String certificateName;
    private final String password;

    public TLSSocketFactory(String certificatePath, String certificateName,String password) {
        this.certificatePath = certificatePath;
        this.certificateName =  certificateName;
        this.password = password;
    }

    public SSLContext getSSLContext() throws Exception {
        String certificateAbsolutePath = Paths.get(certificatePath, certificateName).toAbsolutePath().toString();
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(certificateAbsolutePath), password.toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);

        return sslContext;
    }


}
