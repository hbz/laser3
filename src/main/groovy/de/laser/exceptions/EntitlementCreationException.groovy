package de.laser.exceptions

import groovy.transform.CompileStatic
import org.springframework.validation.Errors

@CompileStatic
class EntitlementCreationException extends Exception {

    EntitlementCreationException(Errors errors) {
        super(errors.toString())
    }

    EntitlementCreationException(String defMessage) {
        super(defMessage)
    }

}
