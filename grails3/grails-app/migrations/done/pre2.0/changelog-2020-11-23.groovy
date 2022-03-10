databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1606120913379-1") {
        grailsChange {
            change {
                String query = "update refdata_value set rdv_value = 'laser' where rdv_value = 'semanticUI' and rdv_owner = " +
                        "(select rdc_id from refdata_category where rdc_description = 'user.setting.theme')"

                sql.execute( query )
            }
            rollback {}
        }
    }
}