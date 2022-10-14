package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1663066067663-1") {
        createIndex(indexName: "ie_status_accept_status_idx", tableName: "issue_entitlement") {
            column(name: "ie_accept_status_rv_fk")

            column(name: "ie_subscription_fk")

            column(name: "ie_status_rv_fk")
        }
    }
}
