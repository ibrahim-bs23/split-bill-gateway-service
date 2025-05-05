package com.brainstation.ib.gateway.domain.dto;

import com.brainstation.ib.gateway.domain.enums.RowStatus;
import lombok.Data;

@Data
public class UserApiNotAccessDto {
    private Long id;
    private String url;
    private RowStatus status;
}