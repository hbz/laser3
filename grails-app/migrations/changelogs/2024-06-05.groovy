package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1717582664557-1") {
        createIndex(indexName: "altname_lic_idx", tableName: "alternative_name") {
            column(name: "altname_lic_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1717582664557-2") {
        createIndex(indexName: "altname_org_idx", tableName: "alternative_name") {
            column(name: "altname_org_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1717582664557-3") {
        createIndex(indexName: "altname_pkg_idx", tableName: "alternative_name") {
            column(name: "altname_pkg_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1717582664557-4") {
        createIndex(indexName: "altname_plat_idx", tableName: "alternative_name") {
            column(name: "altname_plat_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1717582664557-5") {
        createIndex(indexName: "altname_prov_idx", tableName: "alternative_name") {
            column(name: "altname_prov_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1717582664557-6") {
        createIndex(indexName: "altname_sub_idx", tableName: "alternative_name") {
            column(name: "altname_sub_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1717582664557-7") {
        createIndex(indexName: "altname_tipp_idx", tableName: "alternative_name") {
            column(name: "altname_tipp_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1717582664557-8") {
        createIndex(indexName: "altname_vendor_idx", tableName: "alternative_name") {
            column(name: "altname_vendor_fk")
        }
    }
}
