package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1692780133207-1") {
        createIndex(indexName: "ci_e_idx", tableName: "cost_item") {
            column(name: "ci_e_fk")
        }
    }
}
