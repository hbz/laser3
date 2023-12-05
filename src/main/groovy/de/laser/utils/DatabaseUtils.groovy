package de.laser.utils

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.RandomStringUtils

@Slf4j
class DatabaseUtils {

    static Map<String, String> getQueryStruct_ilike(String clause, String param) {

        String name     = 'p_' + RandomStringUtils.randomAlphanumeric(6)
        String query    = ' (lower(' + clause + ') like :' + name + ') '
        String value    = param.toLowerCase().trim()

        if (value.startsWith('"')) {
            value = value.substring(1)
            value = '\"' + value
        }
        if (value.endsWith('"')) {
            value = value.substring(0, value.length() - 1)
            value = value + '\"'
        }
        value = '%' + value + '%'

        println 'getQueryStruct_ilike() ' + query + ' # ' + value

        [name: name, value: value, query: query]
    }
}
