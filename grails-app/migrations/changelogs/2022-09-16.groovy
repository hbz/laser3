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

    changeSet(author: "klober (generated)", id: "1663308275560-2") {
        dropForeignKeyConstraint(baseTableName: "wf_task", constraintName: "FKa93gneffyahjs49jjt7315nn1")
    }

    changeSet(author: "klober (generated)", id: "1663308275560-3") {
        dropForeignKeyConstraint(baseTableName: "wf_task_prototype", constraintName: "FKfxc13dlyehreo7pko2ovtigs0")
    }

    changeSet(author: "klober (generated)", id: "1663308275560-4") {
        dropColumn(columnName: "wft_child_fk", tableName: "wf_task")
    }

    changeSet(author: "klober (generated)", id: "1663308275560-5") {
        dropColumn(columnName: "wftp_child_fk", tableName: "wf_task_prototype")
    }
}
