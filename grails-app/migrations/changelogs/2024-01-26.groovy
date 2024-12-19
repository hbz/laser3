package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (hand-coded)", id: "1706287583693-1") {
        grailsChange {
            change {
                int updated = sql.executeUpdate("update org_setting set os_key_enum = 'MAIL_REPLYTO_FOR_SURVEY' where os_key_enum = 'MAIL_FROM_FOR_SURVEY';")
                confirm("update org_setting set os_key_enum = 'MAIL_REPLYTO_FOR_SURVEY' where os_key_enum = 'MAIL_FROM_FOR_SURVEY';: ${updated}")
                changeSet.setComments("update org_setting set os_key_enum = 'MAIL_REPLYTO_FOR_SURVEY' where os_key_enum = 'MAIL_FROM_FOR_SURVEY';: ${updated}")
            }
            rollback {}
        }
    }
}
