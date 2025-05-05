package com.brainstation.ib.gateway.approute;

public class CustomDataConfiguration {
    public CustomDataConfiguration instance() {
        return new CustomDataConfiguration();
    }

    public static final String TOKEN_PREFIX = "Bearer";
    public static final int TOKEN_HEADER_ARRAY_LENGTH = 2;


    public static final String HEADER_TOKEN = "Token";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CURRENT_USER_CONTEXT = "CurrentContext";
    public static final String HEADER_CO_RELATION_ID = "correlationId";
    public final static String HEADER_SESSION_DATA = "Session-Data";
    public static final String HEADER_ENCRYPTED_DATA = "EncryptedData";
}
