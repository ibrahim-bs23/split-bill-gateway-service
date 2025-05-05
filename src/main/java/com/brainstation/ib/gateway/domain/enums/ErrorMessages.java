package com.brainstation.ib.gateway.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorMessages {

    AUTH_HEADER_MISSING("GE4001", "You credentials are messing. Please try again.", HttpStatus.UNAUTHORIZED),
    AUTH_HEADER_MISS_MATCH("GE4001", "You credentials are miss match. Please try again.", HttpStatus.UNAUTHORIZED),
    INVALID_AUTH_TOKEN("GE4001", "You credentials are invalid. Please sign in again.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_RESOURCE_ACCESS("GE4001", "Please provide valid credentials to access this resource", HttpStatus.UNAUTHORIZED),
    DECRYPTION_FAILED("GE4003", "You are not authorized to access this resource", HttpStatus.FORBIDDEN),

    SESSION_TIMEOUT("GE1000", "Your session timeout.", HttpStatus.UNAUTHORIZED),
    SESSION_TIMEOUT_IN_REDIS("GE1001", "Your session timeout.", HttpStatus.UNAUTHORIZED),
    TOKEN_MISS_MATCH_IN_REDIS("GE1002", "Your session timeout.", HttpStatus.PROXY_AUTHENTICATION_REQUIRED),

    GATEWAY_ERROR("GE4022", "Unable to dispatch request at this moment. Please try after sometime.", HttpStatus.BAD_GATEWAY),
    DEVICE_IDENTIFIER_MISMATCHED("GE4025", "This device is not authorized to access this resource.", HttpStatus.FORBIDDEN),

    SESSION_FAILED("GE4026", "Session Data Mismatched", HttpStatus.UNAUTHORIZED),
    TIME_MISMATCHED("GE4027", "Time Mismatched. Please Sync your device Time", HttpStatus.BAD_REQUEST),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
