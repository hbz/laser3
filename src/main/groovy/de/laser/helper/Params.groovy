package de.laser.helper

import de.laser.RefdataValue
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j

@Slf4j
class Params {

    static List<Long> getLongList(GrailsParameterMap params, String key) {
        params.list(key).findAll().collect{ Long.valueOf(it) }
    }

    static List<RefdataValue> getRefdataList(GrailsParameterMap params, String key) {
        params.list(key).findAll().collect{ RefdataValue.get(Long.valueOf(it)) }.findAll()
    }
}
