package org.murmurRelay.infrastructures.dtos;

public class ServerDto {
    private final String domain;
    private final String base64AES;

    public ServerDto(String domain,String base64AES) {
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
