package de.laser.exceptions

import groovy.transform.CompileStatic
import org.springframework.validation.Errors

@CompileStatic
class SyncException extends Exception {

    SyncException (Errors errors) {
        super(errors.toString())
    }

    SyncException (String defMessage) {
        super(defMessage)
    }

}
