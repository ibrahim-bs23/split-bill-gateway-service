package com.brainstation.ib.gateway.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "api_limiter")
@EqualsAndHashCode(callSuper = true)
public class ApiLimiter extends BaseEntity {
    private String path;
    private String method;
    private int threshold;
    private int ttl;
}
