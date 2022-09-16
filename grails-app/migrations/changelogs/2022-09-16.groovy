package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1663308275560-1") {
        grailsChange {
            change {
                sql.executeUpdate("update system_event set se_category = 'SYSTEM' where se_category = 'CRONJOB' and se_token = 'SUS_SEND_MAIL_ERROR'")
            }
            rollback {}
        }
    }
}
