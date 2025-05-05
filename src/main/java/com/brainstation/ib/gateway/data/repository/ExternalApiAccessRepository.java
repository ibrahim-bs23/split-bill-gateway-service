package com.brainstation.ib.gateway.data.repository;

import com.brainstation.ib.gateway.data.entity.ExternalApiAccess;
import com.brainstation.ib.gateway.domain.enums.RowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalApiAccessRepository extends JpaRepository<ExternalApiAccess, Long> {
    List<ExternalApiAccess> findAllByStatus(RowStatus status);
}
