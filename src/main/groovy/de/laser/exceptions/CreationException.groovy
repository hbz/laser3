package de.laser.exceptions

import groovy.transform.CompileStatic
import org.springframework.validation.Errors

@CompileStatic
class CreationException extends Exception {

    CreationException(Errors errors) {
        super(errors.toString())
    }

    CreationException(List errors) {
        super(errors.toListString())
    }

    CreationException(String defMessage) {
        super(defMessage)
    }

}
