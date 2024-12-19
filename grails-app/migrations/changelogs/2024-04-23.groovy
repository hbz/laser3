package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1713848345516-1") {
        grailsChange {
            change {
                sql.executeUpdate("delete from user_setting where us_key_enum = 'SHOW_EDIT_MODE'")
                sql.executeUpdate("delete from user_setting where us_key_enum = 'SHOW_INFO_ICON'")
            }
            rollback {}
        }
    }
    changeSet(author: "klober (modified)", id: "1713848345516-2") {
        grailsChange {
            change {
                sql.executeUpdate("delete from user_setting where us_key_enum = 'DASHBOARD'")
            }
            rollback {}
        }
    }
}
