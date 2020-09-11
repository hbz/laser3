package de.laser

import groovy.util.logging.Log4j

@Log4j
class OrgAccessPointProxy extends OrgAccessPoint{

    String url

    static mapping = {
        includes OrgAccessPoint.mapping
    }

    static constraints = {
    }
}
