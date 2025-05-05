package com.brainstation.ib.gateway.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserType {

    CUSTOMER(1, "Customer"),
    ADMIN(2, "Admin"),
    MERCHANT(3, "Merchant"),
    ;

    private int code;
    private String text;

    public static boolean isCustomer(int userTypeCode) {
        return UserType.CUSTOMER.getCode() == userTypeCode;
    }

    public static boolean isMerchant(int userTypeCode) {
        return UserType.MERCHANT.getCode() == userTypeCode;
    }

    public static boolean isUserTypeValid(int userType) {
        for (UserType type : UserType.values()) {
            if (type.getCode() == userType) return true;
        }
        return false;
    }

    public static boolean isUserTypeNotValid(int userType) {
        return !isUserTypeValid(userType);
    }
}
