package com.brainstation.ib.gateway.data.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "external_api_access")
@EqualsAndHashCode(callSuper = true)
public class ExternalApiAccess extends BaseEntity {
    private String uri;
}
