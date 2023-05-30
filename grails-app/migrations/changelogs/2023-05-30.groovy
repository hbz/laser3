package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1685433904452-1") {
        addColumn(tableName: "user") {
            column(name: "usr_formal_org_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1685433904452-2") {
        addColumn(tableName: "user") {
            column(name: "usr_formal_role_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1685433904452-3") {
        addForeignKeyConstraint(baseColumnNames: "usr_formal_role_fk", baseTableName: "user", constraintName: "FK2kywkmm9j78p6d82si41vjjum", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "r_id", referencedTableName: "role", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1685433904452-4") {
        addForeignKeyConstraint(baseColumnNames: "usr_formal_org_fk", baseTableName: "user", constraintName: "FK7u6g0fgaftqni6uaf7ka3ku8j", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1685433904452-5") {
        dropColumn(columnName: "usr_shibb_scope", tableName: "user")
    }
}
