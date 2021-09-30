databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1632228806219-1") {
        grailsChange {
            change {
                sql.execute("delete from subscription_property where sp_type_fk = (select pd_id from property_definition where pd_name = 'KfL')")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1632228806219-2") {
        grailsChange {
            change {
                sql.execute("delete from property_definition where pd_name = 'KfL'")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1632228806219-3") {
        addColumn(tableName: "issue_entitlement") {
            column(name: "ie_perpetual_access_by_sub_fk", type: "int8")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1632228806219-4") {
        addForeignKeyConstraint(baseColumnNames: "ie_perpetual_access_by_sub_fk", baseTableName: "issue_entitlement", constraintName: "FKgnwnhaj7fnllowveafnqpxwuk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription")
    }


    changeSet(author: "djebeniani (generated)", id: "1632228806219-5") {
        dropColumn(columnName: "ie_has_perpetual_access", tableName: "issue_entitlement")
    }

    changeSet(author: "klober (modified)", id: "1632228806219-6") {
        grailsChange {
            change {
                sql.execute("delete from system_profiler where sp_archive in ('1.3-RC', '1.4-RC', '1.5-RC', '1.6-RC', '2.0-RC', '2.1-RC')")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1632228806219-7") {
        grailsChange {
            change {
                sql.execute("delete from system_profiler where sp_archive in ('1.5', '1.6')")
            }
            rollback {}
        }
    }

}
