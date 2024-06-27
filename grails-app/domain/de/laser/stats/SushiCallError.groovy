package de.laser.stats

import de.laser.Org
import de.laser.Platform

class SushiCallError {

    String customerId
    String requestorId
    Platform platform
    Org org
    String errMess

    static mapping = {
        id column: 'sce_id'
        version column: 'sce_version'
        customerId column: 'sce_customer_id'
        requestorId column: 'sce_requestor_id'
        platform column: 'sce_platform_fk'
        org column: 'sce_org_fk'
        errMess column: 'sce_err_mess'
    }

    static constraints = {
        requestorId (nullable: true)
        errMess (nullable: true)
    }
}
