package de.laser

import de.laser.OrgAccessPoint
import groovy.util.logging.Log4j

@Log4j
class OrgAccessPointVpn extends OrgAccessPoint{

    String url

    static mapping = {
        includes OrgAccessPoint.mapping
    }

    static constraints = {
    }
}
