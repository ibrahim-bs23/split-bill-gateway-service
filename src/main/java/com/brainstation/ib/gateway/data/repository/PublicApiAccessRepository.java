package com.brainstation.ib.gateway.data.repository;

import com.brainstation.ib.gateway.data.entity.PublicApiAccess;
import com.brainstation.ib.gateway.domain.enums.RowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicApiAccessRepository extends JpaRepository<PublicApiAccess, Long> {
    List<PublicApiAccess> findPublicApisByStatus(RowStatus status);
}
