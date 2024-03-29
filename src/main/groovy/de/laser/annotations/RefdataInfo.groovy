package de.laser.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)

@interface RefdataInfo {

    public static final String UNKOWN = '?'
    public static final String GENERIC = 'GENERIC'

    String cat() default 'n/a'
    String i18n() default 'n/a'
}
