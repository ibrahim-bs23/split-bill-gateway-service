package com.brainstation.ib.gateway.data.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "exclude_decrypt")
@EqualsAndHashCode(callSuper = true)
public class DecryptApiAccess extends BaseEntity {
    private String path;
    private String method;
}
