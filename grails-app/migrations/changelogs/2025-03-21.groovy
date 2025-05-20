package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1742543737885-1") {
        addColumn(tableName: "doc") {
            column(name: "doc_ckey", type: "varchar(255)")
        }
    }
}
