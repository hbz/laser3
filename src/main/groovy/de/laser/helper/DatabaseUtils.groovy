package de.laser.helper

import de.laser.storage.BeanStorage
import groovy.sql.Sql

import javax.sql.DataSource

class DatabaseUtils {

    static final String DE_U_CO_PHONEBK_X_ICU   = "de-u-co-phonebk-x-icu"
    static final String EN_US_U_VA_POSIX_X_ICU  = "en-US-u-va-posix-x-icu"

    static Map<String, String> getServerInfo() {

        DataSource dataSource = BeanStorage.getDataSource()
        Sql sql = new Sql(dataSource)

        try {
            [
                server_version : (sql.firstRow('show server_version').values().join(',') ?: 'unkown'),
                server_encoding: (sql.firstRow('show server_encoding').values().join(',') ?: 'unkown')
            ]
        } catch (Exception e) {
            println e.getMessage()
            [ server_version: 'unkown', server_encoding: 'unkown' ]
        }
    }

    static Map<String, List> getTablesCollationInfo() {

        Map<String, List> result = [:]
        DataSource dataSource = BeanStorage.getDataSource()
        Sql sql = new Sql(dataSource)

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
}
