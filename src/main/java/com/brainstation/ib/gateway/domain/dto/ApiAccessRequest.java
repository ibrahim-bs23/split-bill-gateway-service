package com.brainstation.ib.gateway.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class ApiAccessRequest {
    private String path;

    private String method;

    @JsonProperty("public")
    private boolean isPublic;

    private String[] userType;

    private String[] userStatus;
}