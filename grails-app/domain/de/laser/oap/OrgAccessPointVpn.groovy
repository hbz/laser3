package de.laser.oap

/**
 * A Virtual Private Network (VPN) configuration.
 */
class OrgAccessPointVpn extends OrgAccessPoint{

    String url

    static mapping = {
        includes OrgAccessPoint.mapping
    }

    static constraints = {
    }
}
