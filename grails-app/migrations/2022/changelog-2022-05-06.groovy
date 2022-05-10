databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1651834782075-1") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_ns = 'ezb_anchor' where idns_ns = 'EZB anchor'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1651834782075-2") {
        grailsChange {
            change {
                sql.execute("update property_definition set pd_name = 'Ill ZETA electronic forbidden' where pd_name = 'Ill ZETA electronic fobidden'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1651834782075-3") {
        grailsChange {
            change {
                sql.execute("update due_date_object set ddo_oid = replace(ddo_oid, 'de.laser.Survey', 'de.laser.survey.Survey')")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1651834782075-4") {
        dropColumn(columnName: "core_status_start", tableName: "issue_entitlement")
        dropColumn(columnName: "core_status_end", tableName: "issue_entitlement")
    }
}
