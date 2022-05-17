package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1644407125465-1") {
        dropColumn(columnName: "set_defvalue", tableName: "system_setting")
    }
}