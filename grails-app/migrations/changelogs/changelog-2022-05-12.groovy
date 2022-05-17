package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1652352331995-1") {
        createIndex(indexName: "tipp_pkg_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_pkg_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1652352331995-2") {
        createIndex(indexName: "tipp_plat_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_plat_fk")
        }
    }

}
