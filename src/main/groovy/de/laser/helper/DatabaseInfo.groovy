package de.laser.helper

import de.laser.annotations.TrigramIndex
import de.laser.storage.BeanStore
import de.laser.utils.CodeUtils
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty

import javax.sql.DataSource
import java.lang.annotation.Annotation

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
        Sql sql = getSql(dsIdentifier)
        try {
            [
                server_version : (sql.firstRow('show server_version').values().join(',') ?: 'unkown'),
                server_encoding: (sql.firstRow('show server_encoding').values().join(',') ?: 'unkown')
            ]
        } catch (Exception e) {
            [ server_version: 'unkown', server_encoding: 'unkown' ]
        }
        finally { sql.close() }
    }

    /**
     * Gets the processes running on the given database. Defaults to the default database
     * @param dsIdentifier the data source identifier whose database processes should be returned; one of DS_DEFAULT or DS_STORAGE
     * @return a {@link List} of processes currently running, see <a href="https://www.postgresql.org/docs/current/monitoring-stats.html#MONITORING-PG-STAT-ACTIVITY-VIEW">pg_stat_activity()</a> for the row structure
     */
    static List<Map<String, Object>> getDatabaseActivity(String dsIdentifier = DS_DEFAULT) {
        Sql sql = getSql(dsIdentifier)
        try {
            List<GroovyRowResult> rows = sql.rows( 'select * from pg_stat_activity where datname = current_database() order by pid')
            rows.collect{getGroovyRowResultAsMap(it) }
        }
        finally { sql.close() }
    }

    /**
     * Gets collation of the database of the given identifier, defaults to the default data source
     * @param dsIdentifier the data source identifier whose collation to get
     * @return the collation (lc_collate) of the database
     */
    static String getDatabaseCollate(String dsIdentifier = DS_DEFAULT) {
        Sql sql = getSql(dsIdentifier)
        try {
            sql.firstRow('select datcollate from pg_database where datname = current_database()').get('datcollate') as String // postgresql16+
        }
        finally { sql.close() }
    }

    /**
     * Lists the conflict in the database listening to the given identifier, defaults to the default data source
     * @param dsIdentifier the data source identifier of the database whose conflicts should be returned; one of DS_DEFAULT or DS_STORAGE
     * @return a formatted string containing the cancels due to conflicts in the database, see <a href="https://www.postgresql.org/docs/current/monitoring-stats.html#MONITORING-PG-STAT-DATABASE-CONFLICTS-VIEW">pg_stat_database_conflicts()</a> for more
     */
    static String getDatabaseConflicts(String dsIdentifier = DS_DEFAULT) {
        Sql sql = getSql(dsIdentifier)
        try {
            GroovyRowResult row = sql.firstRow('select * from pg_stat_database_conflicts where datname = current_database()')
            row.findAll { it.key.startsWith('confl_') }.collect { it -> it.key.replace('confl_', '') + ':' + it.value }.join(', ')
        }
        finally { sql.close() }
    }

    /**
     * Gets the size of the requested database, defaults to the default data source
     * @param dsIdentifier the data source identifier of the database whose size should be retrieved
     * @return the size of the requested database
     */
    static String getDatabaseSize(String dsIdentifier = DS_DEFAULT) {
        Sql sql = getSql(dsIdentifier)
        try {
            sql.firstRow('select pg_size_pretty(pg_database_size(current_database())) as dbsize').get('dbsize') as String
        }
        finally { sql.close() }
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
        Map<String, List> result = [:]

        Sql sql = getSql(dsIdentifier)
        try {
            if (sql.firstRow("select 1 from information_schema.tables where table_catalog = current_database() and table_schema = 'public' and table_name = 'pg_stat_statements'")) {
                String hql = """
                    select queryid, calls, total_time, min_time, max_time, mean_time, query from pg_stat_statements where queryid is not null 
                    and query not like '%pg_%' and query not in ('BEGIN', 'COMMIT', 'SELECT \$1')
                    """

                result.calls = sql.rows(hql + " order by calls desc limit 15").collect{getGroovyRowResultAsMap(it) }
                result.maxTime = sql.rows(hql + " order by max_time desc limit 15").collect{getGroovyRowResultAsMap(it) }
            }
        }
        finally { sql.close() }

        result
    }

    /**
     * Retrieves the user-defined functions in the database listening to the given identifier; defaults to the default data source
     * @param dsIdentifier the data source identifier of the database whose user functions should be retrieved; one of DS_DEFAULT or DS_STORAGE
     * @return a {@link List} of rows containing the user-defined functions in the given database
     */
    static List<Map<String, Object>> getDatabaseUserFunctions(String dsIdentifier = DS_DEFAULT) {
        Sql sql = getSql(dsIdentifier)
        try {
            List<GroovyRowResult> rows = sql.rows( """
                select routine_name as function, trim(split_part(split_part(routine_definition, ';', 1), '=', 2)) as version from information_schema.routines 
                where routine_schema = 'public' and routine_type = 'FUNCTION' and external_name is null order by function;
                """)
            rows.collect{getGroovyRowResultAsMap(it) }
        }
        finally { sql.close() }
    }

    static List<Map<String, Object>> getDatabaseExtensions(String dsIdentifier = DS_DEFAULT) {
        Sql sql = getSql(dsIdentifier)
        try {
            List<GroovyRowResult> rows = sql.rows( 'select extname as name, extversion as version from pg_extension')
            rows.collect{getGroovyRowResultAsMap(it) }
        }
        finally { sql.close() }
    }

    static String getMaxConnections(String dsIdentifier = DS_DEFAULT) {
        Sql sql = getSql(dsIdentifier)
        try {
            sql.firstRow('show max_connections')[0] as String
        }
        finally { sql.close() }
    }

    static String getStatementTimeout(String dsIdentifier = DS_DEFAULT) {
        Sql sql = getSql(dsIdentifier)
        try {
            sql.firstRow('show statement_timeout')[0] as String
        }
        finally { sql.close() }
    }

    /**
     * Gets all tables with their respective collation and indices in the given database. Default is the default data source
     * @param dsIdentifier the data source identifier of the database whose tables should be retrieved; one of DS_DEFAULT or DS_STORAGE
     * @return a {@link List} of tables defined in the given database
     */
    static List<Map<String, Object>> getAllTablesWithCollations(String dsIdentifier = DS_DEFAULT) {
        Sql sql = getSql(dsIdentifier)
        try {
            List<GroovyRowResult> rows = sql.rows("""
                select table_schema, table_name, column_name, data_type, collation_catalog, collation_schema, collation_name, indexdetails as index_details, indexname as index_name from (
                    select table_schema, table_name, column_name, data_type, collation_catalog, collation_schema, collation_name
                        from information_schema.columns
                        where data_type in ('text', 'character varying') and table_schema = 'public'
                ) as tc left join (
                    select schemaname, tablename, substring(indexdef from position('(' in indexdef)) as indexdetails, indexname
                        from pg_indexes
                        where schemaname = 'public' and indexdef like 'CREATE INDEX%'
                ) as idx
                on (table_name = tablename and indexdetails like concat('%', column_name, '%'))
                order by table_schema, table_name, column_name, indexname;
                """)

            rows.collect{getGroovyRowResultAsMap(it) }
        }
        finally { sql.close() }
    }

    static List<List> getAllTablesWithGORMIndices() {
        List<List> result = []

        Sql sql = getSql(DS_DEFAULT)
        try {
            def sf = BeanStore.get('sessionFactory')
            int i = 0

            CodeUtils.getAllDomainClasses().each { cls ->
                String clstable  = null
                try {
                    clstable = sf.getClassMetadata(cls).getTableName()
                } catch (Exception e) {
                    clstable = null
                }
                PersistentEntity pe = CodeUtils.getPersistentEntity(cls.name)
                if (pe) {
                    Map mapping = pe.mapping?.mappedForm?.columns
                    if (mapping) {
                        mapping.sort().each { prop ->
                            PersistentProperty pp = pe.getPropertyByName(prop.key)
                            prop.value.columns.each { c ->
                                if (c.index) {
                                    List<GroovyRowResult> siList = []
                                    c.index.split(',').each { ci ->
                                        String query = """
                                            select pg_size_pretty(pg_relation_size(indexrelid)) "idx_size", idx_scan from pg_stat_all_indexes idx join pg_class c on idx.relid = c.oid
                                            where idx.relname='${clstable}' and indexrelname = '${ci.trim()}'"""
                                        siList << (clstable ? sql.firstRow(query) : null)
                                    }
                                    result << [i++, cls.name, pp.name, pp.type, c.name, c.index, siList]
                                }
                                Annotation ti = pp.reader.field().getAnnotation(TrigramIndex)
                                if (ti) {
                                    List<GroovyRowResult> siList = []
                                    ti.index().split(',').each { tti ->
                                        String query = """
                                            select pg_size_pretty(pg_relation_size(indexrelid)) "idx_size", idx_scan from pg_stat_all_indexes idx join pg_class c on idx.relid = c.oid
                                            where idx.relname='${clstable}' and indexrelname = '${tti.trim()}'"""
                                        siList << (clstable ? sql.firstRow(query) : null)
                                    }
                                    result << [i++, cls.name, pp.name, pp.type, c.name, ti.index(), siList]

                                }
                                if (!c.index && !ti) {
                                    result << [i++, cls.name, pp.name, pp.type, c.name, (prop.value.unique ? 'UNIQUE' : null), null]
                                }
                            }
                        }
                    }
                }
            }
        }
        finally { sql.close() }

        result
    }

    /**
     * Gets the count of rows for all tables defined in the given database. Default is the default data source
     * @param dsIdentifier the data source identifier of the database whose table usages should be retrieved; one of DS_DEFAULT or DS_STORAGE
     * @return a {@link List} of rows containing every table and the row count in each
     */
    static List<Map<String, Object>> getAllTablesUsageInfo(String dsIdentifier = DS_DEFAULT) {
        Sql sql = getSql(dsIdentifier)
        try {
            List<GroovyRowResult> rows = sql.rows( "select relname as tablename, reltuples as rowcount from pg_class join information_schema.tables on relname = table_name where table_schema = 'public' order by table_name")
            rows.collect{getGroovyRowResultAsMap(it) }
        }
        finally { sql.close() }
    }

    /**
     * Gets all tables with their columns and their respective collation in the given database. Default is the default data source
     * @param dsIdentifier the data source identifier of the database whose tables should be retrieved; one of DS_DEFAULT or DS_STORAGE
     * @return a {@link List} of tables and columns defined in the given database
     */
    static Map<String, List> getAllTablesCollationInfo(String dsIdentifier = DS_DEFAULT) {
        Map<String, List> result = [:]

        Sql sql = getSql(dsIdentifier)
        try {
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
        }
        finally { sql.close() }

        result
    }

    static List<String> getDbmVersion(String dsIdentifier = DS_DEFAULT) {
        Sql sql = getSql(dsIdentifier)
        try {
            sql.firstRow('SELECT filename, id, dateexecuted from databasechangelog order by orderexecuted desc limit 1').collect { it.value } as List<String>
        }
        finally { sql.close() }
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

    static Sql getSql(String dsIdentifier) {
        DataSource dataSource = getDataSource(dsIdentifier)
        new Sql(dataSource)
    }
}
