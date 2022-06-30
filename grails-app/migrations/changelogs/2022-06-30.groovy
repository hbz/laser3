package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1656573365140-1") {
        dropTable(tableName: "content_item")
    }
}
