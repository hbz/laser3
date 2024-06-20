package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1718880478502-1") {
        dropForeignKeyConstraint(baseTableName: "org", constraintName: "fk1aee448a545b3")
    }

    changeSet(author: "klober (generated)", id: "1718880478502-2") {
        dropColumn(columnName: "org_sector_rv_fk", tableName: "org")
    }
}
