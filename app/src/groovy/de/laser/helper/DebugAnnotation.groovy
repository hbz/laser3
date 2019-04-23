package de.laser.helper

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
    String[] specRoles() default []
}
