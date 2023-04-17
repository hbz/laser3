package de.laser.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target([ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)

@interface DebugInfo {

    public static final int NOT_TRANSACTIONAL  = 0
    public static final int IN_BETWEEN         = 1
    public static final int WITH_TRANSACTION   = 2

    // accessService

    String[] ctxPermAffiliation() default []                                    // [orgPerms, instUserRole]
    String[] ctxInstUserCheckPerm_or_ROLEADMIN() default []                     // [orgPerms]
    String[] ctxInstEditorCheckPerm_or_ROLEADMIN() default []                   // [orgPerms]
    String[] ctxInstAdmCheckPerm_or_ROLEADMIN() default []                      // [orgPerms]
    String[] ctxConsortiumCheckPermAffiliation_or_ROLEADMIN() default []        // [orgPerms, instUserRole]

    // user

    String[] hasCtxAffiliation_or_ROLEADMIN() default []                        // [instUserRole]

    // legacy

    int ctrlService() default 0
    // NOT_TRANSACTIONAL    - no use of associated controllerService
    // IN_BETWEEN           - logic is partially in controllerService
    // WITH_TRANSACTION     - full use of associated controllerService

    int wtc() default 0
    // NOT_TRANSACTIONAL    - no use of withTransaction{}
    // IN_BETWEEN           - partially wrapped with withTransaction{}
    // WITH_TRANSACTION     - full wrapped with withTransaction{}
}
