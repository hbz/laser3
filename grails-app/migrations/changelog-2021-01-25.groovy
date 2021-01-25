databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1611561146351-1") {
        dropColumn(columnName: "date_actioned", tableName: "user_org")
    }

    changeSet(author: "klober (generated)", id: "1611561146351-2") {
        dropColumn(columnName: "date_requested", tableName: "user_org")
    }

    changeSet(author: "klober (generated)", id: "1611561146351-3") {
        dropColumn(columnName: "status", tableName: "user_org")
    }
}