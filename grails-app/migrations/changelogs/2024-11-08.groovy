package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1731051888385-1") {
        dropForeignKeyConstraint(baseTableName: "org", constraintName: "FKk3uxe03uscg8ifybv8w6r4lvd")
    }

    changeSet(author: "klober (generated)", id: "1731051888385-2") {
        dropColumn(columnName: "org_type_rv_fk", tableName: "org")
    }
}
