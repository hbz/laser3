databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1665558394948-1") {
        createIndex(indexName: "c4r_report_per_inst_idx", tableName: "counter4report") {
            column(name: "c4r_report_from")

            column(name: "c4r_report_institution_guid")

            column(name: "c4r_title_guid")
        }
    }

    changeSet(author: "galffy (generated)", id: "1665558394948-2") {
        createIndex(indexName: "c5r_report_per_inst_idx", tableName: "counter5report") {
            column(name: "c5r_report_from")

            column(name: "c5r_report_institution_guid")

            column(name: "c5r_title_guid")
        }
    }
}
