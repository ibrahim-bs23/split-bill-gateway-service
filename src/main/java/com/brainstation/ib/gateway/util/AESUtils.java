package com.brainstation.ib.gateway.util;


import com.brainstation.ib.gateway.model.ExternalTokenDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@UtilityClass
public class AESUtils {
    public static final String ENCODING = "UTF-8";
    public static final String KEY_ALGORITHM = "AES";
    public static final String DIGEST_ALGORITHM = "SHA-256";
    public static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";


    public static SecretKeySpec secretKey(final String mySecret) {
        try {
            byte[] key;
            MessageDigest sha;
            key = mySecret.getBytes(ENCODING);
            sha = MessageDigest.getInstance(DIGEST_ALGORITHM);
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, KEY_ALGORITHM);
        } catch (Exception ex) {
            log.error("Error during set secret : " + ex);
            return null;
        }
    }

    public static String encrypt(final String dataToEncrypt, final String secret) {
        if (ObjectUtils.isEmpty(dataToEncrypt)) {
            return null;
        }
        try {
            final Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(secret));
            final byte[] encryptedBytes = cipher.doFinal(dataToEncrypt.getBytes(ENCODING));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes);
        } catch (Exception ex) {
            log.error("Error while encrypting:  " + ex);
        }
        return null;
    }


    public static String decrypt(final String encryptedData, final String secret) {
        if (ObjectUtils.isEmpty(encryptedData)) {
            return null;
        }
        try {
            final Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey(secret));
            final byte[] decryptedBytes = cipher.doFinal(Base64.getUrlDecoder().decode(encryptedData));
            return new String(decryptedBytes, ENCODING);
        } catch (Exception ex) {
            log.error("Error during decrypting : " + ex);
        }
        return null;
    }


    public static ExternalTokenDto extractToken(String token, String secret) {
        try {
            final String decryptToken = decrypt(token, secret);
            return JacksonUtil.objectMapper().readValue(decryptToken, ExternalTokenDto.class);
        } catch (JsonProcessingException ex) {
            log.error("Error during extractToken : " + ex);
        }
        return null;
    }

}
