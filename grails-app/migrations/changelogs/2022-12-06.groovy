package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1670338391348-1") {
        dropForeignKeyConstraint(baseTableName: "doc_context", constraintName: "fk30eba9a858752a7e")
    }

    changeSet(author: "klober (generated)", id: "1670338391348-2") {
        dropColumn(columnName: "dc_rv_doctype_fk", tableName: "doc_context")
    }
}
