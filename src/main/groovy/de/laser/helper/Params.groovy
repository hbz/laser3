package de.laser.helper

import de.laser.RefdataValue
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils

@Slf4j
class Params {

    static List<Long> getLongList(GrailsParameterMap params, String key) {
        params.list(key).findAll().collect{ Long.valueOf(it) }
    }

    static List<Long> getLongList(LinkedHashMap map, String key) {
        List result = []

        if (map.containsKey(key)) {
            if (map.get(key) instanceof List) {
                result = map.get(key).findAll().collect{ Long.valueOf(it) }
            }
            else {
                if (map.get(key)) {
                    result.add(Long.valueOf(map.get(key)))
                }
            }
        }
        result
    }

    static List<RefdataValue> getRefdataList(GrailsParameterMap params, String key) {
        getLongList(params, key).collect{ RefdataValue.get(it) }
    }

    static List<RefdataValue> getRefdataList(LinkedHashMap map, String key) {
        getLongList(map, key).collect{ RefdataValue.get(it) }
    }

    // ---

    static void test() {

        GrailsWebRequest grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        GrailsParameterMap gpm = grailsWebRequest.params

        gpm.test1 = 1
        gpm.test2 = [1, 2, 3, 4, null]
        gpm.test3 = ['10', '20', '30', '40', null]
        gpm.test4 = []
        gpm.test5 = [null]
        gpm.test6 = null
        gpm.test7 = ''

        println '--- GrailsParameterMap ---'

        println getLongList(gpm, 'test1')
        println getLongList(gpm, 'test2')
        println getLongList(gpm, 'test3')
        println getLongList(gpm, 'test4')
        println getLongList(gpm, 'test5')
        println getLongList(gpm, 'test6')
        println getLongList(gpm, 'test7')
        println getLongList(gpm, 'test999')

        println getRefdataList(gpm, 'test2')
        println getRefdataList(gpm, 'test3')

        Map map = new LinkedHashMap()

        map.test1 = 1
        map.test2 = [1, 2, 3, 4, null]
        map.test3 = ['10', '20', '30', '40', null]
        map.test4 = []
        map.test5 = [null]
        map.test6 = null
        map.test7 = ''

        println '--- LinkedHashMap ---'

        println getLongList(map, 'test1')
        println getLongList(map, 'test2')
        println getLongList(map, 'test3')
        println getLongList(map, 'test4')
        println getLongList(map, 'test5')
        println getLongList(map, 'test6')
        println getLongList(map, 'test7')
        println getLongList(map, 'test999')

        println getRefdataList(map, 'test2')
        println getRefdataList(map, 'test3')
    }
}
