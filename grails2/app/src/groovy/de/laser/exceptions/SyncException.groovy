package de.laser.exceptions

import org.springframework.validation.Errors

class SyncException extends Exception {

    SyncException (Errors errors) {
        super(errors.toString())
    }

    SyncException (String defMessage) {
        super(defMessage)
    }

}
