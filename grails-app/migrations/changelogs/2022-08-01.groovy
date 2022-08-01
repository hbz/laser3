databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1659345821169-6") {
        grailsChange {
            change {
                sql.execute("TRUNCATE TABLE audit_log RESTART IDENTITY")
            }
            rollback {}
        }
    }
}
