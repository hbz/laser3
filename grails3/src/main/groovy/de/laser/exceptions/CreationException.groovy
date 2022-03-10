package de.laser.exceptions

import org.springframework.validation.Errors

class CreationException extends Exception {

    CreationException(Errors errors) {
        super(errors.toString())
    }

    CreationException(String defMessage) {
        super(defMessage)
    }

}
