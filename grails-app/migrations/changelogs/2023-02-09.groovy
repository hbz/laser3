package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1675931878786-1") {
        addColumn(tableName: "org_access_point") {
            column(name: "oar_note", type: "text")
        }
    }
}
