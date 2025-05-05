package com.brainstation.ib.gateway.data.repository;

import com.brainstation.ib.gateway.data.entity.ApiRoute;
import com.brainstation.ib.gateway.domain.enums.RowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiRouteRepository extends JpaRepository<ApiRoute, Long> {
    List<ApiRoute> findAllByStatus(RowStatus status);
}
