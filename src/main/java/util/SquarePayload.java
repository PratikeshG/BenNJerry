package util;

import java.io.Serializable;

import org.jasypt.util.text.BasicTextEncryptor;

public class SquarePayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private String merchantId;
    private String locationId;
    private String encryptedAccessToken;
    private String encryptedRefreshToken;
    private String merchantAlias;
    private String startOfSeason; // TODO: TNT specific - this should not be here

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getEncryptedAccessToken() {
        return encryptedAccessToken;
    }

    public void setEncryptedAccessToken(String encryptedAccessToken) {
        this.encryptedAccessToken = encryptedAccessToken;
    }

    public String getEncryptedRefreshToken() {
        return encryptedRefreshToken;
    }

    public void setEncryptedRefreshToken(String encryptedRefreshToken) {
        this.encryptedRefreshToken = encryptedRefreshToken;
    }

    public String getMerchantAlias() {
        return merchantAlias;
    }

    public void setMerchantAlias(String merchantAlias) {
        this.merchantAlias = merchantAlias;
    }

    public String getStartOfSeason() {
        return startOfSeason;
    }

    public void setStartOfSeason(String startOfSeason) {
        this.startOfSeason = startOfSeason;
    }

    public String encryptToken(String token, String encryptionKey) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(encryptionKey);
        return textEncryptor.encrypt(token);
    }

    public String decryptToken(String encryptedToken, String encryptionKey) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(encryptionKey);
        return textEncryptor.decrypt(encryptedToken);
    }

    public void encryptAccessToken(String accessToken, String encryptionKey) {
        this.encryptedAccessToken = encryptToken(accessToken, encryptionKey);
    }

    public void encryptRefreshToken(String refreshToken, String encryptionKey) {
        this.encryptedRefreshToken = encryptToken(refreshToken, encryptionKey);
    }

    public String getAccessToken(String encryptionKey) {
        return decryptToken(this.encryptedAccessToken, encryptionKey);
    }

    public String getRefreshToken(String encryptionKey) {
        return decryptToken(this.encryptedRefreshToken, encryptionKey);
    }

}
