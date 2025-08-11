package de.laser.stats

import de.laser.Org
import de.laser.exceptions.CreationException
import de.laser.wekb.Platform

@Deprecated
class CounterCheck {

    Long timestamp
    String customerId
    String requestorId
    Platform platform
    String url
    Org org
    boolean callError = false
    String errMess
    String errToken

    static mapping = {
        id          column: 'cc_id'
        version     column: 'cc_version'
        timestamp   column: 'cc_timestamp'
        customerId  column: 'cc_customer_id', index: 'cc_customer_idx, cc_customer_requestor_platform_idx'
        requestorId column: 'cc_requestor_id', index: 'cc_requestor_idx, cc_customer_requestor_platform_idx'
        platform    column: 'cc_platform_fk', index: 'cc_platform_idx, cc_customer_requestor_platform_idx'
        url         column: 'cc_url', type: 'text'
        org         column: 'cc_org_fk', index: 'cc_org_idx'
        callError   column: 'cc_call_error'
        errMess     column: 'cc_err_mess'
        errToken    column: 'cc_err_token'
    }

    static constraints = {
        customerId  (nullable: true)
        requestorId (nullable: true)
        org         (nullable: true)
    }

    static CounterCheck construct(Map configMap) throws CreationException {
        configMap.timestamp = System.currentTimeMillis()
        CounterCheck cc = new CounterCheck(configMap)
        if(!cc.save())
            throw new CreationException(cc.getErrors().getAllErrors())
        else cc
    }
}
