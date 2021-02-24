databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1614160052993-1") {
        grailsChange {
            change {
                sql.execute("delete from role where authority='ORG_INST_COLLECTIVE' and role_type='org'")
            }
            rollback {}
        }
    }
}
