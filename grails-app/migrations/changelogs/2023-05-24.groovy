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

    changeSet(author: "galffy (generated)", id: "1684906588477-5") {
        createIndex(indexName: "ie_perpetual_access_by_sub_idx", tableName: "issue_entitlement") {
            column(name: "ie_perpetual_access_by_sub_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1684906588477-6") {
        createIndex(indexName: "pt_ie_idx", tableName: "permanent_title") {
            column(name: "pt_ie_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1684906588477-7") {
        createIndex(indexName: "pt_owner_idx", tableName: "permanent_title") {
            column(name: "pt_owner_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1684906588477-8") {
        createIndex(indexName: "pt_subscription_idx", tableName: "permanent_title") {
            column(name: "pt_subscription_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1684906588477-9") {
        createIndex(indexName: "pt_tipp_idx", tableName: "permanent_title") {
            column(name: "pt_tipp_fk")
        }
    }
}
