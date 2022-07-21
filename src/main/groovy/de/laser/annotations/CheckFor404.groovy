package de.laser.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

@interface CheckFor404 {
    static final String KEY = 'CheckFor404'

    String[] alternatives() default []
    String label() default ''
}
