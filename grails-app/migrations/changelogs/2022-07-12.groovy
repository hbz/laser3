package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1657610295746-1") {
        dropColumn(columnName: "or_title_fk", tableName: "org_role")
    }
}
