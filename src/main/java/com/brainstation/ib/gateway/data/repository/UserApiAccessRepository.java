package com.brainstation.ib.gateway.data.repository;

import com.brainstation.ib.gateway.data.entity.UserApiAccess;
import com.brainstation.ib.gateway.domain.enums.UserStatus;
import com.brainstation.ib.gateway.domain.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserApiAccessRepository extends JpaRepository<UserApiAccess, Long> {
    List<UserApiAccess> findAllByUserTypeAndUserStatus(UserType userType, UserStatus userStatus);

    boolean existsByUrlAndUserTypeAndUserStatus(String url, UserType userType, UserStatus userStatus);
}
