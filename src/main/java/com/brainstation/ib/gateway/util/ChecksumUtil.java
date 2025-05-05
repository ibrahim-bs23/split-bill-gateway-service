package com.brainstation.ib.gateway.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;

@Slf4j
@UtilityClass
public class ChecksumUtil {

    public static String createChecksum(String data) {
        try {
            final MessageDigest digest = MessageDigest.getInstance(AESUtils.DIGEST_ALGORITHM);
            final byte[] hash = digest.digest(data.getBytes());

            final StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                final String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            log.error("Error while createChecksum:  " + ex);
            return null;
        }

    }

    public static boolean verifyChecksum(String inputRawData, String checksum) {
        try {
            String calculatedChecksum = createChecksum(inputRawData);
            return checksum.equals(calculatedChecksum);
        } catch (Exception ex) {
            log.error("Error while verifyChecksum:  " + ex);
            return false;
        }
    }
}
