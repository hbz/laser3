package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1729238598216-1") {
        grailsChange {
            change {
                String query = 'update "user" set usr_email = \'todo@todo.todo\' where (usr_email is null or usr_email = \'\')'
                sql.execute(query)

                String info = query + ' -> ' + sql.getUpdateCount()
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "klober (generated)", id: "1729238598216-2") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "usr_email", tableName: "user", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1729238598216-3") {
        dropForeignKeyConstraint(baseTableName: "dashboard_due_date", constraintName: "fk2de8a86474c97df9")
    }

    changeSet(author: "klober (generated)", id: "1729238598216-4") {
        dropColumn(columnName: "das_responsible_org_fk", tableName: "dashboard_due_date")
    }
}
