package de.laser.annotations

import java.lang.annotation.*

@Documented
@Target([ElementType.FIELD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)

@interface UIDoc {

    String usage() default ''
}
