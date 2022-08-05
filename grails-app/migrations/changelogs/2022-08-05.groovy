databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1659682856959-1") {
        addColumn(tableName: "system_event") {
            column(name: "se_has_changed", type: "boolean")
        }
    }

    changeSet(author: "klober (modified)", id: "1659682856959-2") {
        grailsChange {
            change {
                sql.execute("update system_event set se_has_changed = false where se_has_changed is null")

            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1659682856959-3") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "se_has_changed", tableName: "system_event", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1659682856959-4") {
        grailsChange {
            change {
                sql.execute("delete from system_activity_profiler where date_part('year', sap_date_created) = '2021'")
                sql.execute("delete from system_event where date_part('year', se_created) = '2021'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1659682856959-5") {
        grailsChange {
            change {
                sql.execute("delete from system_profiler where sp_archive in ('2.1', '2.1-RC', '2.2-RC', '2.3-RC')")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1659682856959-6") {
        grailsChange {
            change {
                sql.execute("delete from system_event where se_token = 'YODA_ES_RESET_END'")
                sql.execute("delete from system_event where se_token = 'YODA_ES_RESET_DROP_OK'")
                sql.execute("delete from system_event where se_token = 'YODA_ES_RESET_CREATE_OK'")
            }
            rollback {}
        }
    }
}