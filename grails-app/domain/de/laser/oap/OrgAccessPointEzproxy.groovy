package de.laser.oap

/**
 * An EZProxy (<a href="https://www.oclc.org/en/ezproxy.html">OCLC EZProxy documentation</a>) configuration.
 */
class OrgAccessPointEzproxy extends OrgAccessPoint{

    String url

    static mapping = {
        includes OrgAccessPoint.mapping
        url            column:'oar_url'
    }

    static constraints = {
    }
}
