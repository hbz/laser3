package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1669888402654-1") {
        addColumn(tableName: "platform") {
            column(name: "plat_central_api_key", type: "text")
        }
    }
}
