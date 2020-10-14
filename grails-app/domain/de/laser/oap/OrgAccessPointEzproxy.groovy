package de.laser.oap

import groovy.util.logging.Slf4j

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
