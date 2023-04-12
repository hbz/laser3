package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (hand-coded)", id: "1680680702702-1") {
        grailsChange {
            change {
                sql.execute("update property_definition set pd_tenant_fk = null where pd_tenant_fk = 1 and pd_description = 'Subscription Property' and pd_name = 'Steuersatz'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1680680702702-2") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_ns = 'viaf' where idns_ns = 'VIAF'")
            }
            rollback {}
        }
    }
}