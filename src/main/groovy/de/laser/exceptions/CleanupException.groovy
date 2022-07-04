package de.laser.exceptions

import groovy.transform.CompileStatic
import org.springframework.validation.Errors

@CompileStatic
class CleanupException extends Exception {

    CleanupException(Errors errors) {
        super(errors.toString())
    }

    CleanupException(String defMessage) {
        super(defMessage)
    }

}
