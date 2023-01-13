databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1673434536709-1") {
        createIndex(indexName: "ie_sub_tipp_status_idx", tableName: "issue_entitlement") {
            column(name: "ie_tipp_fk")

            column(name: "ie_subscription_fk")

            column(name: "ie_status_rv_fk")
        }
    }
}
