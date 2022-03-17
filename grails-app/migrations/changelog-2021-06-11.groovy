databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1623389474012-1") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_ns = 'DBS-ID' where idns_ns = 'dbis_org_id'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623389474012-2") {
        grailsChange {
            change {
                sql.execute("delete from identifier where id_ns_fk in (select idns_id from identifier_namespace where idns_ns = 'global')")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623389474012-3") {
        grailsChange {
            change {
                sql.execute("delete from identifier_namespace where idns_ns = 'global'")
            }
            rollback {}
        }
    }

}
