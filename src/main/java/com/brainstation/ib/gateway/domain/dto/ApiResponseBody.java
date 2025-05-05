package com.brainstation.ib.gateway.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@RequiredArgsConstructor
public class ApiResponseBody<T> implements Serializable {
    private String responseCode;
    private String responseMessage;
    private T data;
}
