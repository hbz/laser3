package com.k_int.kbplus

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
