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

}
