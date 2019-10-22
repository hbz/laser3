package com.k_int.kbplus

import groovy.util.logging.Log4j

@Log4j
class OrgAccessPointEzproxy extends OrgAccessPoint{

    String url

    static mapping = {
        includes OrgAccessPoint.mapping
        url            column:'oar_url'
    }

    static constraints = {
    }
}
