package de.laser.helper

import de.laser.RefdataValue
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils


@Slf4j
class Params {

    // takes String or Long; removes 0, null and empty values
    static List<Long> getLongList(GrailsParameterMap params, String key) {
        params.list(key).findAll().collect{ Long.valueOf(it) }.findAll()
    }

    // takes String or Long; removes 0, null and empty values
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
        result.findAll()
    }

    // takes String or Long; removes 0, null and empty values
    static List<RefdataValue> getRefdataList(GrailsParameterMap params, String key) {
        getLongList(params, key).collect{ RefdataValue.get(it) }
    }

    // takes String or Long; removes 0, null and empty values
    static List<RefdataValue> getRefdataList(LinkedHashMap map, String key) {
        getLongList(map, key).collect{ RefdataValue.get(it) }
    }

    // takes String; removes 0, null and empty values
    static List<Long> getLongList_forCommaSeparatedString(GrailsParameterMap params, String key) {
        List result = []

        if (params.get(key)) {
            result = params.get(key).split(',').collect{
                if (it.trim() && it.trim() != 'null') { Long.valueOf(it.trim()) }
            }.findAll()
        }
        result
    }

    // takes String; removes 0, null and empty values
    static List<Long> getLongList_forCommaSeparatedString(LinkedHashMap map, String key) {
        List result = []

        if (map.get(key)) {
            result = map.get(key).split(',').collect{
                if (it.trim() && it.trim() != 'null') { Long.valueOf(it.trim()) }
            }.findAll()
        }
        result
    }

//    // takes String; removes 0, null and empty values
//    static List<Long> getLongList_forCommaSeparatedOIDString(GrailsParameterMap params, String key) {
//        List result = []
//
//        if (params.get(key)) {
//            result = params.get(key).split(',').collect{
//                if (it.trim() && it.contains(':')) {
//                    String id = it.trim().split(':')[1]
//                    if (id != 'null') {
//                        Long.valueOf(id)
//                    }
//                }
//            }.findAll()
//        }
//        result
//    }
//
//    // takes String; removes 0, null and empty values
//    static List<Long> getLongList_forCommaSeparatedOIDString(LinkedHashMap map, String key) {
//        List result = []
//
//        if (map.get(key)) {
//            result = map.get(key).split(',').collect{
//                if (it.trim() && it.contains(':')) {
//                    String id = it.trim().split(':')[1]
//                    if (id != 'null') {
//                        Long.valueOf(id)
//                    }
//                }
//            }.findAll()
//        }
//        result
//    }

    // ---

    static void test() {

        Map map = new LinkedHashMap()

        map.test0 = 0
        map.test1 = 1
        map.test2 = [0, 1, 2, 3, 4, null]
        map.test3 = ['0', '10', '20', '30', '40', null]
        map.test4 = []
        map.test5 = [0, '0', null]
        map.test6 = null
        map.test7 = 'null'
        map.test8 = ''
        map.test9 = [0, '33 ', '0', ' 34 ', 'null', ' 35', null, '36', '']
        map.test10 = '0, 55, 66,77 , ,88'
        map.test11 = '0, 55, 66,77 ,null,99'
        map.test12 = '0'

//        map.test20 = 'de.laser.Org:1, de.laser.Org:2 , test:3 ,blubb_4,de.laser.Org:null,,null'
//        map.test30 = [0, 1990, null, Year.parse('1991'), '0', '1992', 'null', ' 1993 ', '']

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

//        Closure test_gll_fcsoids = { key ->
//            def a = getLongList_forCommaSeparatedOIDString(gpm, key)
//            def b = getLongList_forCommaSeparatedOIDString(map, key)
//
//            if (a.equals(b)) {
//                println 'OK     #' + key + '     ' + a + ' == ' + b
//            } else {
//                println 'FAILED #' + key + '     ' + a + ' != ' + b
//            }
//        }

        println '--- getLongList ---'
        test_gll('test1')
        test_gll('test0')
        test_gll('test2')
        test_gll('test3')
        test_gll('test4')
        test_gll('test5')
        test_gll('test6')
        // test_gll('test7') // String 'null' --> NumberFormatException ; TODO
        test_gll('test8')
        // test_gll('test9') // --> multiple NumberFormatException ; TODO
        test_gll('test12')
        test_gll('test99999')

        println '--- getRefdataList ---'
        test_grdl('test0')
        test_grdl('test2')
        test_grdl('test3')
        test_grdl('test12')

        println '--- getLongList_byCommaSeparatedString ---'
        test_gll_fcss('test6')
        test_gll_fcss('test7')
        test_gll_fcss('test8')
        test_gll_fcss('test10')
        test_gll_fcss('test11')
        test_gll_fcss('test11')

//        println '--- getLongList_forCommaSeparatedOIDString ---'
//        test_gll_fcsoids('test6')
//        test_gll_fcsoids('test7')
//        test_gll_fcsoids('test8')
//        test_gll_fcsoids('test20')
    }
}
