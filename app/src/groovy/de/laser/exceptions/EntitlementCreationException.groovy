package de.laser.exceptions

import org.springframework.validation.Errors

class EntitlementCreationException extends Exception {

    EntitlementCreationException(Errors errors) {
        super(errors.toString())
    }

    EntitlementCreationException(String defMessage) {
        super(defMessage)
    }

}
