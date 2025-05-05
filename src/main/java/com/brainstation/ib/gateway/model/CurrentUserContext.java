package com.brainstation.ib.gateway.model;

import com.brainstation.ib.gateway.domain.enums.UserStatus;
import com.brainstation.ib.gateway.domain.enums.UserType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class CurrentUserContext implements Serializable {
    private String email;
    private String phoneNumber;
    private String countryCallingCode;
    private String userIdentity;
    private String username;
    private UserType userType;
    private String userGroupCode;
    private UserStatus userStatus;
    private String productTypeCode;
    private String coRelationId;
    private List<String> scope;
    private String branchCode;
}
