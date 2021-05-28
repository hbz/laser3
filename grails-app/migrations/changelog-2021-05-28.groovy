databaseChangeLog = {

    //this changeset is needed because platformStatus current would come to late otherwise
    changeSet(author: "galffy (hand-coded)", id: "1622197154325-1") {
        grailsChange {
            change {
                sql.execute("insert into refdata_value (rdv_version, rdv_is_hard_data, rdv_owner, rdv_value, rdv_date_created, rdv_last_updated) values " +
                        "(1, true, (select rdc_id from refdata_category where rdc_description = 'platform.status'), 'Current', now(), now())")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1622197154325-2") {
        grailsChange {
            change {
                sql.execute("update platform set plat_status_rv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Current' and rdc_description = 'platform.status') where plat_status_rv_fk is null")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1622197154325-3") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "plat_status_rv_fk", tableName: "platform")
    }
}
