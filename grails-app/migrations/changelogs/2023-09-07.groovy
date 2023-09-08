package changelogs

databaseChangeLog = {


    changeSet(author: "djebeniani (generated)", id: "1694097702152-1") {
        addColumn(tableName: "subscription") {
            column(name: "sub_reminder_sent", type: "boolean") {
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1694097702152-2") {
        addColumn(tableName: "subscription") {
            column(name: "sub_reminder_sent_date", type: "timestamp")
        }
    }

    changeSet(author: "djebeniani (modified)", id: "1694097702152-3") {
        grailsChange {
            change {
                sql.execute("UPDATE subscription set sub_reminder_sent = false WHERE sub_reminder_sent is null")
            }
            rollback {}
        }
    }
}
