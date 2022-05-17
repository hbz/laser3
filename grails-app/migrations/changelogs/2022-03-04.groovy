package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1646399535286-1") {
        addColumn(tableName: "platform") {
            column(name: "plat_title_namespace", type: "text")
        }
    }

}
