package de.laser.annotations

import java.lang.annotation.*

@Documented
@Target([ElementType.FIELD, ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)

@interface Fixed_DoNotModify {
}
