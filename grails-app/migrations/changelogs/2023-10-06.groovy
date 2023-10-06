package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1696574287091-1") {
        createIndex(indexName: "doc_content_type_idx", tableName: "doc") {
            column(name: "doc_content_type")
        }
    }
}
