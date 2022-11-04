package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1667544925944-1") {
        addColumn(tableName: "survey_org") {
            column(name: "surorg_reminder_mail_date", type: "timestamp")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1667544925944-2") {
        addColumn(tableName: "survey_config") {
            column(name: "surconf_comment_for_new_participants", type: "text")
        }
    }
}
