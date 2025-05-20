package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1743075714167-1") {
        grailsChange {
            change {
                sql.execute("delete from user_setting where us_key_enum = 'SHOW_SIMPLE_VIEWS'")
                String info = 'UserSetting$SHOW_SIMPLE_VIEWS deleted: ' + sql.getUpdateCount()
                confirm(info)
                changeSet.setComments(info)
            }
        }
    }
}
