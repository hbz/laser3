package de.laser.annotations

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Documented
@Target([ElementType.FIELD, ElementType.LOCAL_VARIABLE])
@Retention(RetentionPolicy.RUNTIME)

@interface TrigramIndex {
    String index() default ''
    String lower() default ''
}
