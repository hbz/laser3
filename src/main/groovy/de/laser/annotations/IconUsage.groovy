package de.laser.annotations

import java.lang.annotation.*

@Documented
@Target([ElementType.FIELD, ElementType.TYPE])
@Retention(RetentionPolicy.RUNTIME)

@interface IconUsage {

    String usage() default ''
}
