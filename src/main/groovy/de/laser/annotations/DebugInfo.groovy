package de.laser.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target([ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)

@interface DebugInfo {

    static final int NOT_TRANSACTIONAL  = 0
    static final int IN_BETWEEN         = 1
    static final int WITH_TRANSACTION   = 2

    String test() default ''
    String perm() default ''
    String type() default ''
    String affil() default ''
    String specRole() default ''

    int ctrlService() default 0
    // NOT_TRANSACTIONAL    - no use of associated controllerService
    // IN_BETWEEN           - logic is partially in controllerService
    // WITH_TRANSACTION     - full use of associated controllerService

    int wtc() default 0
    // NOT_TRANSACTIONAL    - no use of withTransaction{}
    // IN_BETWEEN           - partially wrapped with withTransaction{}
    // WITH_TRANSACTION     - full wrapped with withTransaction{}
}
