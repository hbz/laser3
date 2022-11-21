databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1668003694232-1") {
        addColumn(tableName: "counter4report") {
            column(name: "c4r_yop", type: "timestamp")
        }
    }

    changeSet(author: "galffy (generated)", id: "1668003694232-2") {
        addColumn(tableName: "counter5report") {
            column(name: "c5r_yop", type: "timestamp")
        }
    }

    changeSet(author: "galffy (generated)", id: "1668003694232-3") {
        createIndex(indexName: "c4r_yop_idx", tableName: "counter4report") {
            column(name: "c4r_yop")
        }
    }

    changeSet(author: "galffy (generated)", id: "1668003694232-4") {
        createIndex(indexName: "c5r_yop_idx", tableName: "counter5report") {
            column(name: "c5r_yop")
        }
    }
}
