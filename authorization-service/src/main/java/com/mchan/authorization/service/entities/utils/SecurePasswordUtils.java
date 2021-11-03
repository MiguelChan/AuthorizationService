package com.mchan.authorization.service.entities.utils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * Utilities used for securing a given password.
 */
@Component
public class SecurePasswordUtils {

    private final String pepperValue;
    private final int numberOfIterations;

    @Autowired
    public SecurePasswordUtils(@Qualifier("pepperValue") String pepperValue,
                               @Qualifier("bcryptIterations") int numberOfIterations) {
        this.pepperValue = pepperValue;
        this.numberOfIterations = numberOfIterations;
    }

    /**
     * Creates a SecurePassword that can be used to be stored in the Database.
     * This method returns a {@link String} that is the encrypted password.
     * These two attributes are meant to be stored in the Database for further usage.
     *
     * @param originalPassword .
     *
     * @return A string that contains the encrypted password.
     *
     * @throws Exception .
     */
    public String createSecurePassword(String originalPassword) throws Exception {
        // 1.- We encode using a Pepper.
        String pepperedPassword = addPepper(originalPassword);
        // 2.- We generate a Salt
        String passwordSalt = BCrypt.gensalt(numberOfIterations);
        // 3-. We hash everything we currently have.
        return BCrypt.hashpw(pepperedPassword, passwordSalt);
    }

    /**
     * Validates that the provided password is valid.
     *
     * @param originalPassword .
     *
     * @param encryptedPassword  .
     *
     * @return .
     *
     * @throws Exception .
     */
    public boolean isValidPassword(String originalPassword, String encryptedPassword) throws Exception {
        String pepperedPassword = addPepper(originalPassword);
        return BCrypt.checkpw(pepperedPassword, encryptedPassword);
    }

    private String addPepper(String originalPassword) throws Exception {
        byte[] keyBites = pepperValue.getBytes();
        SecretKey secretKey = new SecretKeySpec(keyBites, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");

        mac.init(secretKey);

        byte[] hashedPassword = originalPassword.getBytes();

        return new String(Base64.encodeBase64(mac.doFinal(hashedPassword))).trim();
    }

}
