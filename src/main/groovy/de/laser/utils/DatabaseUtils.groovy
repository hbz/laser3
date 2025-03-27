package de.laser.utils

import groovy.util.logging.Slf4j

/**
 * Helper class for database query related tasks
 */
@Slf4j
class DatabaseUtils {

    /**
     * Translates the field into a case-insensitive fuzzy database query argument
     * @param field the field to translate
     * @param value the named parameter used in the query
     * @return a {@link Map} in structure {query, name, value}
     */
    static Map<String, String> getQueryStruct_ilike(String field, String value) {

        String name     = 'p_' + RandomUtils.getAlphaNumeric(6)
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

        log.debug('getQueryStruct_ilike(field, value) -> ' + query + ' ? ' + val)
        [query: query, name: name, value: val]
    }

    /**
     * Translates the given list of fields into a case-insensitive fuzzy database query argument
     * @param fields the list of field to translate
     * @param value the named parameter used in the query
     * @return a {@link Map} in structure {query, name, value}
     */
    static Map<String, Object> getQueryStruct_ilike(List<String> fields, String value) {

        String name     = 'p_' + RandomUtils.getAlphaNumeric(6)
        String query    = ''
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

        List<String> subQueries = []
        fields.each { field ->
            subQueries.add(' (lower(' + field + ') like :' + name + ') ')
        }
        query = '(' + subQueries.join('or') + ')'

        log.debug('getQueryStruct_ilike(List fields, value) -> ' + query + ' ? ' + val)
        [query: query, name: name, value: val]
    }
}
