package de.laser.oap

import groovy.util.logging.Slf4j

/**
 * A Shibboleth (https://shibboleth.atlassian.net/wiki/home) configuration.
 */
@Slf4j
class OrgAccessPointShibboleth extends OrgAccessPoint{

    String entityId

    static mapping = {
        includes OrgAccessPoint.mapping
        entityId        column:'oar_entity_id'
    }

    static constraints = {
    }
}
