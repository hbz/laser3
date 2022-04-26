databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1650975495869-1") {

        grailsChange {
            change {
                String roles = "( 'ROLE_API', 'ROLE_GLOBAL_DATA', 'ROLE_PACKAGE_EDITOR', 'ROLE_TICKET_EDITOR' )"
                sql.execute("delete from user_role where role_id in ( select id from role where authority in " + roles + " )")
                sql.execute("delete from role where authority in " + roles)
            }
            rollback {}
        }
    }


}
