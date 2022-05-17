package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1652272047755-1") {
        grailsChange {
            change {
                sql.execute("delete from refdata_value where rdv_is_hard_data = false and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'tax.type')")
            }
            rollback {}
        }
    }

}
