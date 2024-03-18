package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1710751368877-22") {
        dropForeignKeyConstraint(baseTableName: "task", constraintName: "fk36358511779714")
    }

    changeSet(author: "klober (generated)", id: "1710751368877-24") {
        dropColumn(columnName: "tsk_pkg_fk", tableName: "task")
    }

}
