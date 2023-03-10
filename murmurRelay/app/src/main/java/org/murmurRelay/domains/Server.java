package org.murmurRelay.domains;

import java.net.InetAddress;

public class Server {
    private final String domain;
    private final String base64AES;
    private InetAddress ipAddress;
    private int port;

    public Server(String domain,String base64AES) {
        this.domain = domain;
        this.base64AES = base64AES;
    }

    public Server(String domain,String base64AES,InetAddress ipAddress,int port) {
        this.domain = domain;
        this.base64AES = base64AES;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public int getPort() {return port;}

    public InetAddress getIpAddress() {return ipAddress;}

    public String getDomain() {
        return domain;
    }

    public String getBase64AES() {
        return base64AES;
    }
}
