import de.laser.helper.DatabaseUtils

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1649658426259-1") {
        grailsChange {
            change {
                String collate = DatabaseUtils.DE_U_CO_PHONEBK_X_ICU
                String locale = collate.replace('-x-icu', '')
                sql.execute('create collation if not exists public."' + collate + '" (provider = icu, locale = "' + locale + '")')
            }
            rollback {}
        }
    }
    changeSet(author: "klober (modified)", id: "1649658426259-2") {
        grailsChange {
            change {
                String collate = DatabaseUtils.EN_US_U_VA_POSIX_X_ICU
                String locale = collate.replace('-x-icu', '')
                sql.execute('create collation if not exists public."' + collate + '" (provider = icu, locale = "' + locale + '")')
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1649658426259-3") {
        grailsChange {
            change {
                List<List> todo = [
                        ['due_date_object', 'ddo_attribute_value', 'varchar(255)', false],
                        ['i10n_translation', 'i10n_value', 'text', false],
                        ['identifier_namespace', 'idns_description', 'text', false],
                        ['identifier_namespace', 'idns_name', 'varchar(255)', false],
                        ['property_definition', 'pd_name', 'varchar(255)', false],
                        ['property_definition', 'pd_explanation', 'text', false],
                        ['refdata_category', 'rdc_description', 'varchar(255)', true],
                        ['refdata_value', 'rdv_value', 'varchar(255)', true],
                        ['refdata_value', 'rdv_explanation', 'text', false],
                        ['system_message', 'sm_content', 'text', false]
                ]
                todo.each { nfo ->
                    String table = nfo[0]
                    String column_de = nfo[1] + '_de'
                    String column_en = nfo[1] + '_en'
                    String type = nfo[2]
                    boolean indexUpdate = nfo[3]

                    sql.execute('alter table ' + table + ' alter column ' + column_de + ' type ' + type + ' collate public."' + DatabaseUtils.DE_U_CO_PHONEBK_X_ICU + '"')
                    if (indexUpdate) {
                        sql.execute('drop index ' + column_de + '_idx')
                        sql.execute('create index ' + column_de + '_idx on ' + table + '(' + column_de + ')')
                    }

                    sql.execute('alter table ' + table + ' alter column ' + column_en + ' type ' + type + ' collate public."' + DatabaseUtils.EN_US_U_VA_POSIX_X_ICU + '"')
                    if (indexUpdate) {
                        sql.execute('drop index ' + column_en + '_idx')
                        sql.execute('create index ' + column_en + '_idx on ' + table + '(' + column_en + ')')
                    }
                }
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1649658426259-4") {
        grailsChange {
            change {
                sql.execute("delete from system_profiler where sp_archive = '2.0'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (generated)", id: "1649658426259-5") {
        dropIndex(indexName: "rdv_owner_value_idx", tableName: "refdata_value")
    }

    changeSet(author: "klober (generated)", id: "1649658426259-6") {
        createIndex(indexName: "rdv_owner_idx", tableName: "refdata_value") {
            column(name: "rdv_owner")
        }
    }

    changeSet(author: "klober (generated)", id: "1649658426259-7") {
        createIndex(indexName: "rdv_value_idx", tableName: "refdata_value") {
            column(name: "rdv_value")
        }
    }

    changeSet(author: "klober (modified)", id: "1649658426259-8") {
        createIndex(indexName: "rdv_owner_value_idx", tableName: "refdata_value") {
            column(name: "rdv_owner")
            column(name: "rdv_value")
        }
    }

    changeSet(author: "klober (generated)", id: "1649658426259-9") {
        dropIndex(indexName: "se_created_idx", tableName: "system_event")
    }

    changeSet(author: "klober (generated)", id: "1649658426259-10") {
        createIndex(indexName: "se_category_idx", tableName: "system_event") {
            column(name: "se_category")
        }
    }
    changeSet(author: "klober (generated)", id: "1649658426259-11") {
        createIndex(indexName: "se_relevance_idx", tableName: "system_event") {
            column(name: "se_relevance")
        }
    }

    changeSet(author: "klober (generated)", id: "1649658426259-12") {
        createIndex(indexName: "sp_archive_idx", tableName: "system_profiler") {
            column(name: "sp_archive")
        }
    }

    changeSet(author: "klober (generated)", id: "1649658426259-13") {
        createIndex(indexName: "sp_context_idx", tableName: "system_profiler") {
            column(name: "sp_context_fk")
        }
    }
}
