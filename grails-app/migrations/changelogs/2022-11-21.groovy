package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1669012951976-1") {
        addColumn(tableName: "issue_entitlement") {
            column(name: "ie_notes", type: "text")
        }
    }
}
