package de.laser.exceptions

import groovy.transform.CompileStatic
import org.springframework.validation.Errors

@CompileStatic
class ChangeAcceptException extends Exception {

    ChangeAcceptException(Errors errors) {
        super(errors.toString())
    }

    ChangeAcceptException(String defMessage) {
        super(defMessage)
    }

}
