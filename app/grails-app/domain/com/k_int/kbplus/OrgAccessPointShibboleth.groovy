package com.k_int.kbplus

import groovy.util.logging.Log4j

@Log4j
class OrgAccessPointShibboleth extends OrgAccessPoint{

    String entityId

    static mapping = {
        includes OrgAccessPoint.mapping
        entityId        column:'oar_entity_id'
    }

    static constraints = {
    }
}
