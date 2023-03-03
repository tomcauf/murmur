package org.murmurRelay.domains;

public class Server {
    private final String domain;
    private final String base64AES;

    public Server(String domain,String base64AES) {
        this.domain = domain;
        this.base64AES = base64AES;
    }

    public String getDomain() {
        return domain;
    }

    public String getBase64AES() {
        return base64AES;
    }
}
