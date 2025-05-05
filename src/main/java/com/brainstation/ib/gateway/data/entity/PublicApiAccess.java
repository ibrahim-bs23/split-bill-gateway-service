package com.brainstation.ib.gateway.data.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "public_api_access")
@EqualsAndHashCode(callSuper = true)
public class PublicApiAccess extends BaseEntity {
    private String url;
}
