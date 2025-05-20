package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1722411300864-1") {
        addColumn(tableName: "license_property") {
            column(name: "lp_paragraph_number", type: "varchar(255)")
        }
    }
}
