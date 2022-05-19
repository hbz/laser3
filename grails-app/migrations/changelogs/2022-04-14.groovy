package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1649930940849-1") {
        createIndex(indexName: "id_instanceof_idx", tableName: "identifier") {
            column(name: "id_instance_of_fk")
        }
    }

}
