package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1716799011485-1") {
        grailsChange {
            change {
                sql.execute("delete from refdata_value where rdv_value='agency' and rdv_owner = (select rdc_id from refdata_category where rdc_description='workflow.workflow.target.type')")
                confirm("refdata_value removed: ${sql.getUpdateCount()}")
            }
            rollback {}
        }
    }

}
