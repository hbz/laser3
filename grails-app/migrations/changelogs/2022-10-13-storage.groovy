package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1665645149130-1") {
        createIndex(indexName: "c4r_title_when_idx", tableName: "counter4report") {
            column(name: "c4r_report_from")

            column(name: "c4r_platform_guid")

            column(name: "c4r_report_institution_guid")

            column(name: "c4r_title_guid")

            column(name: "c4r_report_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1665645149130-2") {
        createIndex(indexName: "c5r_title_when_idx", tableName: "counter5report") {
            column(name: "c5r_report_from")

            column(name: "c5r_platform_guid")

            column(name: "c5r_report_institution_guid")

            column(name: "c5r_title_guid")

            column(name: "c5r_report_type")
        }
    }
}
