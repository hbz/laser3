package de.laser.helper

import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j

@Slf4j
class Params {

    static List<Long> getLongList(GrailsParameterMap params, String key) {
        params.list(key).findAll().collect{ Long.valueOf(it) }
    }
}
