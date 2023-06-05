package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1685511561865-1") {
        createIndex(indexName: "sp_pkg_idx", tableName: "subscription_package") {
            column(name: "sp_pkg_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1685511561865-2") {
        createIndex(indexName: "sp_sub_idx", tableName: "subscription_package") {
            column(name: "sp_sub_fk")
        }
    }
}
