package de.laser.oap

import groovy.util.logging.Slf4j

/**
 * An OpenAthens (https://www.openathens.net/) configuration.
 */
@Slf4j
class OrgAccessPointOA extends OrgAccessPoint{

    String entityId

    static mapping = {
        includes OrgAccessPoint.mapping
        entityId        column:'oar_entity_id'
    }

    static constraints = {
    }
}
