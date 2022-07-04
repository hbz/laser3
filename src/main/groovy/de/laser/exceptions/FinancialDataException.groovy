package de.laser.exceptions

import groovy.transform.CompileStatic

@CompileStatic
class FinancialDataException extends Exception {

    FinancialDataException(String defMessage) {
        super(defMessage)
    }

}
