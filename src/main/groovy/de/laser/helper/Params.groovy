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

    static List<Long> getLongList_forCommaSeparatedString(GrailsParameterMap params, String key) {
        List result = []

        if (params.get(key)) {
            result = params.get(key).split(',').collect{
                if (it.trim() && it.trim() != 'null') { Long.valueOf(it.trim()) }
            }.findAll()
        }
        result
    }

    static List<Long> getLongList_forCommaSeparatedString(LinkedHashMap map, String key) {
        List result = []

        if (map.get(key)) {
            result = map.get(key).split(',').collect{
                if (it.trim() && it.trim() != 'null') { Long.valueOf(it.trim()) }
            }.findAll()
        }
        result
    }

    // ---

    static void test() {

        Map map = new LinkedHashMap()

        map.test1 = 1
        map.test2 = [1, 2, 3, 4, null]
        map.test3 = ['10', '20', '30', '40', null]
        map.test4 = []
        map.test5 = [null]
        map.test6 = null
        map.test7 = 'null'
        map.test8 = ''
        map.test9 = ['33 ', ' 34 ', ' 35', '36', 'null', '', null]
        map.test10 = '55, 66,77 , ,88'
        map.test11 = '55, 66,77 ,null,99'

        GrailsWebRequest grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        GrailsParameterMap gpm = grailsWebRequest.params

        map.each { k, v -> gpm.put(k, v)}


        Closure test_gll = { key ->
            def a = getLongList(gpm, key)
            def b = getLongList(map, key)

            if (a.equals(b)) {
                println 'OK     #' + key + '     ' + a + ' == ' + b
            } else {
                println 'FAILED #' + key + '     ' + a + ' != ' + b
            }
        }

        Closure test_grdl = { key ->
            def a = getRefdataList(gpm, key)
            def b = getRefdataList(map, key)

            if (a.equals(b)) {
                println 'OK     #' + key + '     ' + a + ' == ' + b
            } else {
                println 'FAILED #' + key + '     ' + a + ' != ' + b
            }
        }

        Closure test_gll_fcss = { key ->
            def a = getLongList_forCommaSeparatedString(gpm, key)
            def b = getLongList_forCommaSeparatedString(map, key)

            if (a.equals(b)) {
                println 'OK     #' + key + '     ' + a + ' == ' + b
            } else {
                println 'FAILED #' + key + '     ' + a + ' != ' + b
            }
        }

        println '--- getLongList ---'
        test_gll('test1')
        test_gll('test2')
        test_gll('test3')
        test_gll('test4')
        test_gll('test5')
        test_gll('test6')
        // test_gll('test7') // String 'null' --> NumberFormatException
        test_gll('test8')
        // test_gll('test9') // --> multiple NumberFormatException
        test_gll('test99999')

        println '--- getRefdataList ---'
        test_grdl('test2')
        test_grdl('test3')

        println '--- getLongList_byCommaSeparatedString ---'
        test_gll_fcss('test6')
        test_gll_fcss('test7')
        test_gll_fcss('test8')
        test_gll_fcss('test10')
        test_gll_fcss('test11')
    }
}
