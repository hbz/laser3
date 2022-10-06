package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1664870229820-1") {
        addColumn(tableName: "wf_workflow") {
            column(name: "wfw_user_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1664870229820-2") {
        addForeignKeyConstraint(baseColumnNames: "wfw_user_fk", baseTableName: "wf_workflow", constraintName: "FKkntsihb8ocgr6vqpy21u1n2eu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "usr_id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1664870229820-3") {
        grailsChange {
            change {
                // just keep test data
                sql.executeUpdate('update wf_workflow set wfw_user_fk = (select usr_id from "user" where usr_username = \'klober\')')
            }
            rollback {}
        }
    }

    changeSet(author: "klober (generated)", id: "1664870229820-4") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "wfw_user_fk", tableName: "wf_workflow", validate: "true")
    }
}
