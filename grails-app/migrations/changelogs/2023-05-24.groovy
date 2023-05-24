package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1684906588477-1") {
        createIndex(indexName: "tipp_publisher_name_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_publisher_name")
        }
    }

    changeSet(author: "klober (generated)", id: "1684906588477-2") {
        createIndex(indexName: "tipp_series_name_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_series_name")
        }
    }

    changeSet(author: "klober (generated)", id: "1684906588477-3") {
        createIndex(indexName: "tipp_subject_reference_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_subject_reference")
        }
    }

    changeSet(author: "klober (generated)", id: "1684906588477-4") {
        createIndex(indexName: "tipp_title_type_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_title_type")
        }
    }
}
