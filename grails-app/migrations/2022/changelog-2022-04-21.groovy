databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1650545281075-1") {
        createIndex(indexName: "c4r_report_from_idx", tableName: "counter4report") {
            column(name: "c4r_report_from")
        }
    }

    changeSet(author: "galffy (generated)", id: "1650545281075-2") {
        createIndex(indexName: "c4r_report_to_idx", tableName: "counter4report") {
            column(name: "c4r_report_to")
        }
    }

    changeSet(author: "galffy (generated)", id: "1650545281075-3") {
        createIndex(indexName: "c5r_report_from_idx", tableName: "counter5report") {
            column(name: "c5r_report_from")
        }
    }

    changeSet(author: "galffy (generated)", id: "1650545281075-4") {
        createIndex(indexName: "c5r_report_to_idx", tableName: "counter5report") {
            column(name: "c5r_report_to")
        }
    }

}