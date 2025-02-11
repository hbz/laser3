package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1739263857971-1") {
        dropColumn(columnName: "lic_is_slaved", tableName: "license")
    }

    changeSet(author: "klober (generated)", id: "1739263857971-2") {
        dropColumn(columnName: "sub_is_slaved", tableName: "subscription")
    }
}
