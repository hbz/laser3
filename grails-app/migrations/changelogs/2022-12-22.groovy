databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1671699787512-1") {
        addColumn(tableName: "subscription") {
            column(name: "sub_reference_year", type: "bytea")
        }
    }

    changeSet(author: "galffy (generated)", id: "1671699787512-2") {
        createIndex(indexName: "sub_reference_year_idx", tableName: "subscription") {
            column(name: "sub_reference_year")
        }
    }
}
