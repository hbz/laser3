package de.laser.utils

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.RandomStringUtils

@Slf4j
class DatabaseUtils {

    static Map<String, String> getQueryStruct_ilike(String field, String value) {

        String name     = 'p_' + RandomStringUtils.randomAlphanumeric(6)
        String query    = ' (lower(' + field + ') like :' + name + ') '
        String val      = value.toLowerCase().trim()

        if (val.startsWith('"')) {
            val = val.substring(1)
            val = '\"' + val
        }
        if (val.endsWith('"')) {
            val = val.substring(0, val.length() - 1)
            val = val + '\"'
        }
        val = '%' + val + '%'

        log.debug('getQueryStruct_ilike() -> ' + query + ' ? ' + val)

        [name: name, value: val, query: query]
    }
}
