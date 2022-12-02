databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1668429152907-1") {
        addColumn(tableName: "counter5report") {
            column(name: "c5r_data_type", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1668429152907-2") {
        createIndex(indexName: "c5r_dt_idx", tableName: "counter5report") {
            column(name: "c5r_data_type")
        }
    }
}
