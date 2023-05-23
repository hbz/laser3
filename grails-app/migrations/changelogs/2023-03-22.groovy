package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1679489652455-1") {
        grailsChange {
            change {
                sql.executeUpdate("ALTER TABLE user_org RENAME TO user_org_role")
            }
            rollback {}
        }
    }
}
