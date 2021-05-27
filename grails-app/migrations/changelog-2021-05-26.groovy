databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1622018225727-1") {
        addColumn(tableName: "contact") {
            column(name: "ct_language_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1622018225727-2") {
        grailsChange {
            change {
                sql.execute("update property_definition set pd_name = 'Currency', pd_tenant_fk = null where pd_tenant_fk = (select org_id from org where org_guid = 'org:e6be24ff-98e4-474d-9ef8-f0eafd843d17') and pd_name = 'Währung';")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1622018225727-3") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_ns = 'ezb_sub_id' where idns_ns = 'ezb' and idns_type = 'de.laser.Subscription';")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1622018225727-4") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_ns = 'pissn' where idns_ns = 'issn' and idns_type = 'de.laser.Subscription';")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1622018225727-4") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_ns = 'zdb_pkg' where idns_ns = 'zdb' and idns_type = 'de.laser.Package';")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1622018225727-5") {
        grailsChange {
            change {
                sql.execute("update identifier set id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'zdb' and idns_type = 'de.laser.Subscription') where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ZDB_ID')")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1622018225727-6") {
        grailsChange {
            change {
                sql.execute("delete from identifier_namespace where idns_ns = 'ZDB_ID'")
            }
            rollback {}
        }
    }

}