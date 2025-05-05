package com.brainstation.ib.gateway.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RequestDetails {
    private String method;
    private String path;
}
