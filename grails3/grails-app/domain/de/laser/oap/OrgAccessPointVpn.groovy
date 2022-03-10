package de.laser.oap

import groovy.util.logging.Slf4j

/**
 * A Virtual Private Network (VPN) configuration.
 */
@Slf4j
class OrgAccessPointVpn extends OrgAccessPoint{

    String url

    static mapping = {
        includes OrgAccessPoint.mapping
    }

    static constraints = {
    }
}
