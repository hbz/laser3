package de.laser.exceptions

import org.springframework.validation.Errors

class CleanupException extends Exception {

    CleanupException(Errors errors) {
        super(errors.toString())
    }

    CleanupException(String defMessage) {
        super(defMessage)
    }

}
