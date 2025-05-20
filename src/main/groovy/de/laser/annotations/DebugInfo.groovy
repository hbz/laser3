package de.laser.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target([ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)

@interface DebugInfo {

    public static final int NO  = 0
    public static final int YES = 1

    // contextService

    String[] isInstUser()      default ['']                             // [orgPerms]
    String[] isInstEditor()    default ['']                             // [orgPerms]
    String[] isInstAdm()       default ['']                             // [orgPerms]

    String[] isInstUser_denySupport()      default ['']                 // [orgPerms]
    String[] isInstEditor_denySupport()    default ['']                 // [orgPerms]
    String[] isInstAdm_denySupport()       default ['']                 // [orgPerms]

    String[] isInstEditor_or_ROLEADMIN()    default ['']                // [orgPerms]
    String[] isInstAdm_or_ROLEADMIN()       default ['']                // [orgPerms]

    String[] isInstAdm_denySupport_or_ROLEADMIN()       default ['']    // [orgPerms]

    // legacy

    int ctrlService()       default 0   // associated controllerService in use
    int withTransaction()   default 0   // wrapped logic with withTransaction{}
}
