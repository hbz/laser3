package de.laser.oap


/**
 * A Shibboleth (https://shibboleth.atlassian.net/wiki/home) configuration.
 */
class OrgAccessPointShibboleth extends OrgAccessPoint{

    String entityId

    static mapping = {
        includes OrgAccessPoint.mapping
        entityId        column:'oar_entity_id'
    }

    static constraints = {
    }
}
