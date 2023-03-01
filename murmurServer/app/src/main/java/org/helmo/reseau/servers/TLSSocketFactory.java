package org.helmo.reseau.servers;

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
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(certificateAbsolutePath), password.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext sc = SSLContext.getInstance("TLSv1.3");
        TrustManager[] trustManagers = tmf.getTrustManagers();
        sc.init(kmf.getKeyManagers(), trustManagers, null);

        return sc;
    }


}
