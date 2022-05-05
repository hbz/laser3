package de.laser.oap


/**
 * An OpenAthens (<a href="https://www.openathens.net/">OpenAthens documentation</a>) configuration.
 */
class OrgAccessPointOA extends OrgAccessPoint{

    String entityId

    static mapping = {
        includes OrgAccessPoint.mapping
        entityId        column:'oar_entity_id'
    }

    static constraints = {
    }
}
