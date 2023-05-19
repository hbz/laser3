databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1684498427065-1") {
        createIndex(indexName: "igi_ie_group_idx", tableName: "issue_entitlement_group_item") {
            column(name: "igi_ie_fk")

            column(name: "igi_ie_group_fk")
        }
    }
}
