package de.laser.exceptions

import org.springframework.validation.Errors

class NativeSqlException extends Exception {

    NativeSqlException(Errors errors) {
        super(errors.toString())
    }

    NativeSqlException(List errors) {
        super(errors.toListString())
    }

    NativeSqlException(String defMessage) {
        super(defMessage)
    }

}
