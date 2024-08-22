package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1723540328127-1") {
        createIndex(indexName: "tipp_delayedoae_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_delayedoa_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1723540328127-2") {
        createIndex(indexName: "tipp_hybridoa_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_hybridoa_rv_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1723540328127-3") {
        createIndex(indexName: "tipp_status_reason_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_status_reason_rv_fk")
        }
    }
}
