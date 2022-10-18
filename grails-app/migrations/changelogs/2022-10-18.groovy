package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1666079486075-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "wfw_user_fk", tableName: "wf_workflow")
    }
}
