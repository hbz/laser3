package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1647873127580-1") {
        createIndex(indexName: "ie_sub_tipp_idx", tableName: "issue_entitlement") {
            column(name: "ie_tipp_fk")

            column(name: "ie_subscription_fk")
        }
    }

}
