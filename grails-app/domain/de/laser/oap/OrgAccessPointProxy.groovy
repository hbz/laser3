package de.laser.oap

/**
 * A proxy address configuration.
 */
class OrgAccessPointProxy extends OrgAccessPoint{

    String url

    static mapping = {
        includes OrgAccessPoint.mapping
    }

    static constraints = {
    }
}
