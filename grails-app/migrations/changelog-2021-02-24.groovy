databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1614160052993-1") {
        grailsChange {
            change {
                sql.execute("delete from role where authority='ORG_INST_COLLECTIVE' and role_type='org'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1614160052993-2") {
        grailsChange {
            change {
                sql.execute("delete from refdata_value where rdv_value = 'Department' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'combo.type')")
            }
            rollback {}
        }
    }
}
