package de.laser.utils

import de.laser.GlobalService
import de.laser.storage.BeanStore
import groovy.util.logging.Slf4j
import org.grails.core.artefact.DomainClassArtefactHandler
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory
import org.hibernate.hql.spi.QueryTranslator
import org.hibernate.hql.spi.QueryTranslatorFactory
import org.hibernate.query.Query

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


    static String debugHQL(String hql, Map params) {
        SessionFactory sf = BeanStore.get('sessionFactory') as SessionFactory
        Session session = sf.openSession()

        Query query = session.createQuery(hql)
        params.each{ key, value -> query.setParameter(key, value) }

        QueryTranslatorFactory translatorFactory = new ASTQueryTranslatorFactory()
        QueryTranslator translator = translatorFactory.createQueryTranslator(
                query.getQueryString(),
                query.getQueryString(),
                Collections.EMPTY_MAP,
                sf as SessionFactoryImplementor,
                null
        )
        translator.compile(params, false)

        String sql = translator.getSQLString()
        String exp = ''
        try {
            String tmp = sql
            translator.collectedParameterSpecifications.eachWithIndex{ ps, i ->
                Map.Entry tk = translator.tokenReplacements.find{ it.key == ps.name } as Map.Entry
                tmp = tmp.replaceFirst('\\?', _debugHQL_retoken(tk).toString())
            }
            sql = tmp

            exp = GlobalService.obtainSqlConnection().rows('EXPLAIN (COSTS FALSE) ' + sql).collect{ it.toString().replaceFirst('QUERY PLAN=', '') }.join('\n')
        }
        catch (Exception e) { println e.getMessage() }

        String result = """
+----
-   ${hql}
${params.keySet() ? '-   ' + ('^ ' * params.keySet().size()) + params.toMapString() + '\n-----' : '-----'}
->  ${sql}
-----
${exp}
-----"""
        println result
        result
    }

    static def _debugHQL_retoken(Map.Entry entry) {
        println entry
        def val = entry.value

        if (val == null) {
            return null
        }
        else if (val instanceof List) {
            return val.collect{ val2 -> _debugHQL_retoken([fake: val2].firstEntry()) }.join(', ')
        }
        else if (val instanceof Byte || val instanceof Short || val instanceof Integer || val instanceof Long || val instanceof Float || val instanceof Double) {
            return val
        }
        else if (val instanceof Boolean) {
            return val
        }
        else if (DomainClassArtefactHandler.isDomainClass(val.getClass())) {
            return val.id
        }
        else {
            return "'${val}'"
        }
    }
}
