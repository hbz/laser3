databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1673531702884-1") {
        createIndex(indexName: "tipp_status_pkg_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_pkg_fk")

            column(name: "tipp_status_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1673531702884-2") {
        createIndex(indexName: "tipp_status_plat_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_plat_fk")

            column(name: "tipp_status_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1673531702884-3") {
        createIndex(indexName: "tipp_status_plat_pkg_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_plat_fk")

            column(name: "tipp_pkg_fk")

            column(name: "tipp_status_rv_fk")
        }
    }
}
