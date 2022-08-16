package de.laser.helper

import de.laser.storage.BeanStore
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j

import javax.sql.DataSource

@Slf4j
class DatabaseInfo {

    public static final String DE_U_CO_PHONEBK_X_ICU   = "de-u-co-phonebk-x-icu"
    public static final String EN_US_U_VA_POSIX_X_ICU  = "en-US-u-va-posix-x-icu"

    public static final String DS_DEFAULT = "DS_DEFAULT"
    public static final String DS_STORAGE = "DS_STORAGE"

    static DataSource getDataSource(String dsIdentifier = DS_DEFAULT) {
        if (dsIdentifier == DS_DEFAULT) {
            BeanStore.getDataSource()
        }
        else if (dsIdentifier == DS_STORAGE) {
            BeanStore.getStorageDataSource()
        }
    }

    // --

    static Map<String, String> getServerInfo(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        Sql sql = new Sql(dataSource)

        try {
            [
                server_version : (sql.firstRow('show server_version').values().join(',') ?: 'unkown'),
                server_encoding: (sql.firstRow('show server_encoding').values().join(',') ?: 'unkown')
            ]
        } catch (Exception e) {
            log.error e.getMessage()
            [ server_version: 'unkown', server_encoding: 'unkown' ]
        }
    }

    static List<Map<String, Object>> getDatabaseActivity(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        Sql sql = new Sql(dataSource)

        List<GroovyRowResult> rows = sql.rows( 'select * from pg_stat_activity where datname = current_database() order by pid')
        rows.collect{getGroovyRowResultAsMap(it) }
    }

    static String getDatabaseCollate(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        (new Sql(dataSource)).firstRow('show LC_COLLATE').get('lc_collate') as String
    }

    static String getDatabaseConflicts(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        GroovyRowResult row = (new Sql(dataSource)).firstRow('select * from pg_stat_database_conflicts where datname = current_database()')
        row.findAll { it.key.startsWith('confl_') }.collect { it -> it.key.replace('confl_', '') + ':' + it.value }.join(', ')
    }

    static String getDatabaseSize(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        (new Sql(dataSource)).firstRow('select pg_size_pretty(pg_database_size(current_database())) as dbsize').get('dbsize') as String
    }

    static Map<String, List> getDatabaseStatistics(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        Sql sql = new Sql(dataSource)

        Map<String, List> result = [:]
        if (sql.firstRow("select 1 from information_schema.tables where table_catalog = current_database() and table_schema = 'public' and table_name = 'pg_stat_statements'")) {
            String hql = """
                select queryid, calls, total_time, min_time, max_time, mean_time, query from pg_stat_statements where queryid is not null 
                and query not like '%pg_%' and query not in ('BEGIN', 'COMMIT', 'SELECT \$1')
                """

            result.calls = sql.rows(hql + " order by calls desc limit 15").collect{getGroovyRowResultAsMap(it) }
            result.maxTime = sql.rows(hql + " order by max_time desc limit 15").collect{getGroovyRowResultAsMap(it) }
        }
        result
    }

    static List<Map<String, Object>> getDatabaseUserFunctions(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        Sql sql = new Sql(dataSource)

        List<GroovyRowResult> rows = sql.rows( "select routine_name as function, trim(split_part(split_part(routine_definition, ';', 1), '=', 2)) as version from information_schema.routines where routine_type = 'FUNCTION' and specific_schema = 'public' order by function")
        rows.collect{getGroovyRowResultAsMap(it) }
    }

    static List<Map<String, Object>> getAllTablesWithCollations(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        Sql sql = new Sql(dataSource)

        List<GroovyRowResult> rows = sql.rows("""
            select table_schema, table_name, column_name, data_type, collation_catalog, collation_schema, collation_name,
                (select indexname from pg_indexes where tablename = table_name and indexdef like concat('% INDEX ', column_name, '_idx ON ', table_schema, '.', table_name, ' %')) as index_name
            from information_schema.columns
            where data_type in ('text', 'character varying') and table_schema = 'public'
            order by table_schema, table_name, column_name;
            """)

        rows.collect{getGroovyRowResultAsMap(it) }
    }

    static List<Map<String, Object>> getAllTablesUsageInfo(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        Sql sql = new Sql(dataSource)

        List<GroovyRowResult> rows = sql.rows( "select relname as tablename, reltuples as rowcount from pg_class join information_schema.tables on relname = table_name where table_schema = 'public' order by table_name")
        rows.collect{getGroovyRowResultAsMap(it) }
    }

    static Map<String, List> getAllTablesCollationInfo(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        Sql sql = new Sql(dataSource)
        Map<String, List> result = [:]

        sql.rows( "select tablename from pg_tables where schemaname = 'public'").each { table ->
            String tablename = table.get('tablename')
            List columns = []
            sql.rows("select column_name, data_type, collation_name from information_schema.columns where table_schema = 'public' and table_name = '" + tablename + "'").each{ col ->
                columns.add([
                        column: col.get('column_name'),
                        type: col.get('data_type'),
                        collation: col.get('collation_name') ?: ''
                ])
            }
            result.putAt(tablename, columns)
        }
        result
    }

    static Map<String, Object> getGroovyRowResultAsMap(GroovyRowResult grr) {
        Map<String, Object> row = [:]
        grr.keySet().each{ key -> row.putAt(key as String, grr[key]) }
        row
    }
}
