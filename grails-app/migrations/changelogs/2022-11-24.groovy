package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1669273256246-1") {
        grailsChange {
            change {
                sql.execute("delete from user_role where ur_role_fk = (select r_id from role where r_authority = 'ROLE_ORG_EDITOR')")
                sql.execute("delete from role where r_authority = 'ROLE_ORG_EDITOR'")
            }
            rollback {}
        }
    }
}
