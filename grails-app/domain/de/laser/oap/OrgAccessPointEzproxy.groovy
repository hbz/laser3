package de.laser.oap

import groovy.util.logging.Slf4j

/**
 * An EZProxy (<a href="https://www.oclc.org/en/ezproxy.html">OCLC EZProxy documentation</a>) configuration.
 */
@Slf4j
class OrgAccessPointEzproxy extends OrgAccessPoint{

    String url

    static mapping = {
        includes OrgAccessPoint.mapping
        url            column:'oar_url'
    }

    static constraints = {
    }
}
