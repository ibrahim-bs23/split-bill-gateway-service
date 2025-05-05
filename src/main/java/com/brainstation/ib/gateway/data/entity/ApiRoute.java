package com.brainstation.ib.gateway.data.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.persistence.Table;
import lombok.experimental.Accessors;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "api_route")
@EqualsAndHashCode(callSuper = true)
public class ApiRoute extends BaseEntity {
    private String path;
    private String method;
    private String uri;
}
