package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1753175064591-1") {
        addColumn(tableName: "user") {
            column(name: "usr_inactivity_warning", type: "TIMESTAMP WITHOUT TIME ZONE")
        }
    }
}
