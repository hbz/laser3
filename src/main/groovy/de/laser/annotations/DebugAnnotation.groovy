package de.laser.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target([ElementType.METHOD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)

@interface DebugAnnotation {

    String test() default ''
    String perm() default ''
    String type() default ''
    String affil() default ''
    String specRole() default ''

    int ctrlService() default 0
    // 0: not transactional - no use of associated controllerService
    // 1: in between        - logic is partially in controllerService
    // 2: transactional     - full use of associated controllerService

    int wtc() default 0
    // 0: not transactional - no use of withTransaction{}
    // 1: in between        - partially wrapped with withTransaction{}
    // 2: withTransaction   - full wrapped with withTransaction{}
}
