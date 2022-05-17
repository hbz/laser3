package de.laser.api.v0

import de.laser.RefdataValue
import de.laser.storage.Constants

class ApiBox {

    static final List FAILURE_CODES  = [
            Constants.HTTP_BAD_REQUEST,
            Constants.HTTP_PRECONDITION_FAILED,
            Constants.OBJECT_NOT_FOUND,
            Constants.OBJECT_STATUS_DELETED
    ]

    Object obj
    String status

    /**
     * Initialises a new instance
     */
    static ApiBox get() {
        new ApiBox(obj:null, status:null)
    }

    /**
     * Checks if the requested object exists and if the identifier is unique (i.e. exactly one object matches the identifier)
     */
    void validatePrecondition_1() {
        if (obj) {
            if (obj.size() == 1) {
                obj = obj.get(0)
            }
            else {
                obj = null
                status = Constants.HTTP_PRECONDITION_FAILED
            }
        } else {
            obj = null
            status = Constants.OBJECT_NOT_FOUND
        }
    }

    /**
     * Checks if an object is deleted
     * @param attribute the attribute where deletion is stored
     * @param rdvDeleted the reference data representing the deletion state
     */
    void validateDeletedStatus_2(String attribute, RefdataValue rdvDeleted) {
        if (obj.getProperty(attribute) == rdvDeleted) {
            status = Constants.OBJECT_STATUS_DELETED
        }
    }

    /**
     * Checks one of the failure reasons is given
     * @return true if the status is one of the failure codes, false otherwise
     */
    boolean checkFailureCodes_3() {
        ! (status in ApiBox.FAILURE_CODES)
    }
}

