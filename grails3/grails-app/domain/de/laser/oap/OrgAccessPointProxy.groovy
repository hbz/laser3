package de.laser.oap

import groovy.util.logging.Slf4j

/**
 * A proxy address configuration.
 */
@Slf4j
class OrgAccessPointProxy extends OrgAccessPoint{

    String url

    static mapping = {
        includes OrgAccessPoint.mapping
    }

    static constraints = {
    }
}
