package de.laser.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

@interface Check404 {
    static final String KEY = 'Check404'
    static final String CHECK404_ALTERNATIVES = 'CHECK404_ALTERNATIVES'

    Class domain() default NullPointerException
}
