databaseChangeLog = {

    changeSet(author: "agalffy (modified)", id: "1612442398803-1") {
        grailsChange {
            change {
                sql.execute('update title_instance_package_platform set tipp_title_type = (select rdv_value from refdata_value where rdv_id = tipp_medium_rv_fk) where tipp_title_type is null and tipp_medium_rv_fk is not null')
            }
            rollback {}
        }
    }

}
