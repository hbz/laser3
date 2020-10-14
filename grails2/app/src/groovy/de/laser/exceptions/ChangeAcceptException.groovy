package de.laser.exceptions

import org.springframework.validation.Errors

class ChangeAcceptException extends Exception {

    ChangeAcceptException(Errors errors) {
        super(errors.toString())
    }

    ChangeAcceptException(String defMessage) {
        super(defMessage)
    }

}
