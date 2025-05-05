package com.brainstation.ib.gateway.util;

import java.util.Base64;

public class StringsUtils {

    public static String trim(String string, char ch) {
        String leadingTrimmed = trimLeading(string, ch);
        return trimTrailing(leadingTrimmed, ch);
    }

    public static String trimLeading(String string, char leadingChar) {
        return string.replaceAll("^[" + leadingChar + "]+", "");
    }

    public static String trimTrailing(String string, char trailingChar) {
        return string.replaceAll("[" + trailingChar + "]+$", "");
    }

    public static String toBase64(byte[] byteArray) {
        return Base64.getUrlEncoder().encodeToString(byteArray);
    }
}
