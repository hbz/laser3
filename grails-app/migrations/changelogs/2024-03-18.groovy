package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1710751368877-1") {
        dropForeignKeyConstraint(baseTableName: "task", constraintName: "fk36358511779714")
    }

    changeSet(author: "klober (generated)", id: "1710751368877-2") {
        dropColumn(columnName: "tsk_pkg_fk", tableName: "task")
    }

    changeSet(author: "klober (generated)", id: "1710751368877-3") {
        dropForeignKeyConstraint(baseTableName: "doc_context", constraintName: "fk30eba9a871246d01")
    }

    changeSet(author: "klober (generated)", id: "1710751368877-4") {
        dropColumn(columnName: "dc_pkg_fk", tableName: "doc_context")
    }
}
