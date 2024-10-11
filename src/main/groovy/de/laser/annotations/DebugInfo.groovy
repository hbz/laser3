package de.laser.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target([ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)

@interface DebugInfo {

    public static final int NOT_TRANSACTIONAL  = 0
    public static final int WITH_TRANSACTION   = 2

    // contextService

    String[] isInstUser_or_ROLEADMIN()      default ['']                // [orgPerms]
    String[] isInstEditor_or_ROLEADMIN()    default ['']                // [orgPerms]
    String[] isInstAdm_or_ROLEADMIN()       default ['']                // [orgPerms]

    String[] isInstUser_denySupport_or_ROLEADMIN()      default ['']    // [orgPerms]
    String[] isInstEditor_denySupport_or_ROLEADMIN()    default ['']    // [orgPerms]
    String[] isInstAdm_denySupport_or_ROLEADMIN()       default ['']    // [orgPerms]

    // legacy

    int ctrlService() default 0
    // NOT_TRANSACTIONAL    - no use of associated controllerService
    // WITH_TRANSACTION     - full use of associated controllerService

    int wtc() default 0
    // NOT_TRANSACTIONAL    - no use of withTransaction{}
    // WITH_TRANSACTION     - full wrapped with withTransaction{}
}
