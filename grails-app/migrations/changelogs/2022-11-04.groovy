package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1667544925944-6") {
        addColumn(tableName: "survey_org") {
            column(name: "surorg_reminder_mail_date", type: "timestamp")
        }
    }
}
