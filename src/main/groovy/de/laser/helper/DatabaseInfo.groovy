package de.laser.helper

import de.laser.storage.BeanStore
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j

import javax.sql.DataSource

/**
 * This class keeps information ready about the database instances currently in service
 */
@Slf4j
class DatabaseInfo {

    public static final String DE_U_CO_PHONEBK_X_ICU   = "de-u-co-phonebk-x-icu"
    public static final String EN_US_U_VA_POSIX_X_ICU  = "en-US-u-va-posix-x-icu"

    public static final String DS_DEFAULT = "DS_DEFAULT"
    public static final String DS_STORAGE = "DS_STORAGE"

    /**
     * Returns the connection to the given data source.
     * Defaults to the default data source
     * @param dsIdentifier the data source to which connection should be returned, one of DS_DEFAULT or DS_STORAGE
     * @return the appropriate {@link DataSource}
     */
    static DataSource getDataSource(String dsIdentifier = DS_DEFAULT) {
        if (dsIdentifier == DS_DEFAULT) {
            BeanStore.getDataSource()
        }
        else if (dsIdentifier == DS_STORAGE) {
            BeanStore.getStorageDataSource()
        }
    }

    // --

    /**
     * Gets the information about the given data source; defaults to default data source
     * @param dsIdentifier the data source about which information should be returned, one of DS_DEFAULT or DS_STORAGE
     * @return a {@link Map} containing server_version and server_encoding
     */
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

    /**
     * Gets the processes running on the given database. Defaults to the default database
     * @param dsIdentifier the data source identifier whose database processes should be returned; one of DS_DEFAULT or DS_STORAGE
     * @return a {@link List} of processes currently running, see <a href="https://www.postgresql.org/docs/current/monitoring-stats.html#MONITORING-PG-STAT-ACTIVITY-VIEW">pg_stat_activity()</a> for the row structure
     */
    static List<Map<String, Object>> getDatabaseActivity(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        Sql sql = new Sql(dataSource)

        List<GroovyRowResult> rows = sql.rows( 'select * from pg_stat_activity where datname = current_database() order by pid')
        rows.collect{getGroovyRowResultAsMap(it) }
    }

    /**
     * Gets collation of the database of the given identifier, defaults to the default data source
     * @param dsIdentifier the data source identifier whose collation to get
     * @return the collation (lc_collate) of the database
     */
    static String getDatabaseCollate(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        (new Sql(dataSource)).firstRow('show LC_COLLATE').get('lc_collate') as String
    }

    /**
     * Lists the conflict in the database listening to the given identifier, defaults to the default data source
     * @param dsIdentifier the data source identifier of the database whose conflicts should be returned; one of DS_DEFAULT or DS_STORAGE
     * @return a formatted string containing the cancels due to conflicts in the database, see <a href="https://www.postgresql.org/docs/current/monitoring-stats.html#MONITORING-PG-STAT-DATABASE-CONFLICTS-VIEW">pg_stat_database_conflicts()</a> for more
     */
    static String getDatabaseConflicts(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        GroovyRowResult row = (new Sql(dataSource)).firstRow('select * from pg_stat_database_conflicts where datname = current_database()')
        row.findAll { it.key.startsWith('confl_') }.collect { it -> it.key.replace('confl_', '') + ':' + it.value }.join(', ')
    }

    /**
     * Gets the size of the requested database, defaults to the default data source
     * @param dsIdentifier the data source identifier of the database whose size should be retrieved
     * @return the size of the requested database
     */
    static String getDatabaseSize(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        (new Sql(dataSource)).firstRow('select pg_size_pretty(pg_database_size(current_database())) as dbsize').get('dbsize') as String
    }

    /**
     * Retrieves the statistics of the given database; default is the default data source and the following information is being returned:
     * <ul>
     *     <li>calls</li>
     *     <li>total time</li>
     *     <li>minimum time</li>
     *     <li>maximum time</li>
     *     <li>average time</li>
     *     <li>query</li>
     * </ul>
     * @param dsIdentifier the data source identifier of the database whose statistics should be retrieved; one of DS_DEFAULT or DS_STORAGE
     * @return a {@link List} of rows containing the queries of the given database
     */
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

    /**
     * Retrieves the user-defined functions in the database listening to the given identifier; defaults to the default data source
     * @param dsIdentifier the data source identifier of the database whose user functions should be retrieved; one of DS_DEFAULT or DS_STORAGE
     * @return a {@link List} of rows containing the user-defined functions in the given database
     */
    static List<Map<String, Object>> getDatabaseUserFunctions(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        Sql sql = new Sql(dataSource)

        List<GroovyRowResult> rows = sql.rows( "select routine_name as function, trim(split_part(split_part(routine_definition, ';', 1), '=', 2)) as version from information_schema.routines where routine_type = 'FUNCTION' and specific_schema = 'public' order by function")
        rows.collect{getGroovyRowResultAsMap(it) }
    }

    /**
     * Gets all tables with their respective collation and indices in the given database. Default is the default data source
     * @param dsIdentifier the data source identifier of the database whose tables should be retrieved; one of DS_DEFAULT or DS_STORAGE
     * @return a {@link List} of tables defined in the given database
     */
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

    /**
     * Gets the count of rows for all tables defined in the given database. Default is the default data source
     * @param dsIdentifier the data source identifier of the database whose table usages should be retrieved; one of DS_DEFAULT or DS_STORAGE
     * @return a {@link List} of rows containing every table and the row count in each
     */
    static List<Map<String, Object>> getAllTablesUsageInfo(String dsIdentifier = DS_DEFAULT) {
        DataSource dataSource = getDataSource(dsIdentifier)
        Sql sql = new Sql(dataSource)

        List<GroovyRowResult> rows = sql.rows( "select relname as tablename, reltuples as rowcount from pg_class join information_schema.tables on relname = table_name where table_schema = 'public' order by table_name")
        rows.collect{getGroovyRowResultAsMap(it) }
    }

    /**
     * Gets all tables with their columns and their respective collation in the given database. Default is the default data source
     * @param dsIdentifier the data source identifier of the database whose tables should be retrieved; one of DS_DEFAULT or DS_STORAGE
     * @return a {@link List} of tables and columns defined in the given database
     */
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

    /**
     * Converts the database row into a key-value map
     * @param grr the {@link GroovyRowResult} to process
     * @return a {@link Map} in structure [key: row]
     */
    static Map<String, Object> getGroovyRowResultAsMap(GroovyRowResult grr) {
        Map<String, Object> row = [:]
        grr.keySet().each{ key -> row.putAt(key as String, grr[key]) }
        row
    }
}
