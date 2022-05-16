databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1652424041215-1") {
        createIndex(indexName: "c4r_report_when_idx", tableName: "counter4report") {
            column(name: "c4r_title_fk")

            column(name: "c4r_report_from")

            column(name: "c4r_metric_type")

            column(name: "c4r_report_to")

            column(name: "c4r_report_type")

            column(name: "c4r_report_institution_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1652424041215-2") {
        createIndex(indexName: "c5r_report_when_idx", tableName: "counter5report") {
            column(name: "c5r_title_fk")

            column(name: "c5r_report_from")

            column(name: "c5r_metric_type")

            column(name: "c5r_report_to")

            column(name: "c5r_report_type")

            column(name: "c5r_report_institution_fk")
        }
    }
}
