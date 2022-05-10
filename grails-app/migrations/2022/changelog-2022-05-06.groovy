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

    changeSet(author: "klober (generated)", id: "1651834782075-5") {
        dropColumn(columnName: "doc_status_rv_fk", tableName: "doc")
    }

    changeSet(author: "klober (generated)", id: "1651834782075-6") {
        dropColumn(columnName: "org_membership", tableName: "org")
    }

    changeSet(author: "klober (generated)", id: "1651834782075-7") {
        dropColumn(columnName: "plat_type_rv_fk", tableName: "platform")
    }

    changeSet(author: "klober (generated)", id: "1651834782075-8") {
        dropColumn(columnName: "tipp_option_rv_fk", tableName: "title_instance_package_platform")
        dropColumn(columnName: "tipp_payment_rv_fk", tableName: "title_instance_package_platform")
    }
}
