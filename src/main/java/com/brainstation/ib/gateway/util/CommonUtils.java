package com.brainstation.ib.gateway.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CommonUtils {

    public static String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}
