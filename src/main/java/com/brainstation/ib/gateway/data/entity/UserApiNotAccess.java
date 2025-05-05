package com.brainstation.ib.gateway.data.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "user_api_not_access")
@EqualsAndHashCode(callSuper = true)
public class UserApiNotAccess extends BaseEntity {
    private String url;
}
