databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1645784588629-1") {
        grailsChange {
            change {
                sql.execute("delete from license_property where lp_id in (select lp_id from license_property join refdata_value on lp_ref_value_rv_fk = rdv_id where rdv_value in ('NatHosting PLN', 'NatHosting Portico'))")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1645784588629-2") {
        grailsChange {
            change {
                sql.execute("delete from refdata_value where rdv_value in ('NatHosting PLN', 'NatHosting Portico')")
            }
            rollback {}
        }
    }

}
