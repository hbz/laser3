package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1728392757265-1") {
        addColumn(tableName: "org") {
            column(name: "org_type_rv_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1728392757265-2") {
        addForeignKeyConstraint(baseColumnNames: "org_type_rv_fk", baseTableName: "org", constraintName: "FKk3uxe03uscg8ifybv8w6r4lvd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1728392757265-3") {
        grailsChange {
            change {
                String query = "update org o1 set org_type_rv_fk = tt.refdata_value_id from (select * from org_type) tt where o1.org_id = tt.org_id"
                sql.execute(query)
                String info = query + ' -> ' + sql.getUpdateCount()
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "klober (generated)", id: "1728392757265-4") {
        dropForeignKeyConstraint(baseTableName: "org_type", constraintName: "fke2fae7ab21d4e99d")
    }

    changeSet(author: "klober (generated)", id: "1728392757265-5") {
        dropForeignKeyConstraint(baseTableName: "org_type", constraintName: "fke2fae7abaad0839c")
    }

    changeSet(author: "klober (generated)", id: "1728392757265-6") {
        dropTable(tableName: "org_type")
    }
}
