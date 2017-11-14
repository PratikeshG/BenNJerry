package util;

import java.io.Serializable;

import org.jasypt.util.text.BasicTextEncryptor;

public class SquarePayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private String merchantId;
    private String locationId;
    private String encryptedAccessToken;
    private String merchantAlias;
    private boolean legacySingleLocationSquareAccount;

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

    public String getMerchantAlias() {
        return merchantAlias;
    }

    public void setMerchantAlias(String merchantAlias) {
        this.merchantAlias = merchantAlias;
    }

    public boolean isLegacySingleLocationSquareAccount() {
        return legacySingleLocationSquareAccount;
    }

    public void setLegacySingleLocationSquareAccount(boolean legacy) {
        this.legacySingleLocationSquareAccount = legacy;
    }

    public void encryptAccessToken(String accessToken, String encryptionKey) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(encryptionKey);
        this.encryptedAccessToken = textEncryptor.encrypt(accessToken);
    }

    public String getAccessToken(String encryptionKey) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(encryptionKey);
        return textEncryptor.decrypt(this.encryptedAccessToken);
    }
}
