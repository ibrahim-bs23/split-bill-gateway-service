package com.brainstation.ib.gateway.data.entity;

import com.brainstation.ib.gateway.domain.enums.RowStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RowStatus status = RowStatus.ACTIVE;

    @Column(name = "is_active")
    private boolean active = true;
}
