import groovy.sql.GroovyRowResult

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1654849144524-1") {
        grailsChange {
            change {
                List<GroovyRowResult> rdCheck = sql.rows("select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Removed' and rdc_description = 'tipp.status'")
                if(!rdCheck) {
                    sql.executeInsert("insert into refdata_value (rdv_version, rdv_is_hard_data, rdv_owner, rdv_date_created, rdv_last_updated, rdv_value, rdv_value_de, rdv_value_en) values (0, true, (select rdc_id from refdata_category where rdc_description = 'tipp.status'), now(), now(), 'Removed', 'Entfernt', 'Removed')")
                }
            }
            rollback {}
        }
    }

}
