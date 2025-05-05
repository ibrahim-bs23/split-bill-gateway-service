package com.brainstation.ib.gateway.domain.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserStatus {
    @JsonProperty("ACTIVE")
    ACTIVE,

    @JsonProperty("BLOCK")
    BLOCK,

    @JsonProperty("SUSPEND")
    SUSPEND,

    @JsonProperty("TEMP_BLOCK")
    TEMP_BLOCK,

    @JsonProperty("FORGOT_PASSWORD")
    FORGOT_PASSWORD,

    @JsonProperty("FORCE_PASSWORD_CHANGED")
    FORCE_PASSWORD_CHANGED,

    @JsonProperty("DEVICE_BINDING")
    DEVICE_BINDING,

    @JsonProperty("FORCE_PIN_CHANGE")
    FORCE_PIN_CHANGE,

    @JsonProperty("CHANGE_CREDENTIAL")
    CHANGE_CREDENTIAL
}
