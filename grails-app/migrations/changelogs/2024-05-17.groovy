package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1715939390552-1") {
        addColumn(tableName: "provider") {
            column(name: "prov_inhouse_invoicing", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }
}
