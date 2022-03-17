databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1646220339939-1") {
        createIndex(indexName: "ie_accept_status_idx", tableName: "issue_entitlement") {
            column(name: "ie_accept_status_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1646220339939-2") {
        createIndex(indexName: "ie_medium_idx", tableName: "issue_entitlement") {
            column(name: "ie_medium_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1646220339939-3") {
        createIndex(indexName: "ie_status_idx", tableName: "issue_entitlement") {
            column(name: "ie_status_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1646220339939-4") {
        createIndex(indexName: "tipp_medium_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_medium_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1646220339939-5") {
        createIndex(indexName: "tipp_status_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_status_rv_fk")
        }
    }

}
