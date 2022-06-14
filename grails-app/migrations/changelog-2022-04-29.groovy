databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1651232328540-1") {
        createIndex(indexName: "c4r_metric_type_idx", tableName: "counter4report") {
            column(name: "c4r_metric_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1651232328540-2") {
        createIndex(indexName: "c5r_access_method_idx", tableName: "counter5report") {
            column(name: "c5r_access_method")
        }
    }

    changeSet(author: "galffy (generated)", id: "1651232328540-3") {
        createIndex(indexName: "c5r_access_type_idx", tableName: "counter5report") {
            column(name: "c5r_access_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1651232328540-4") {
        createIndex(indexName: "c5r_metric_type_idx", tableName: "counter5report") {
            column(name: "c5r_metric_type")
        }
    }

}