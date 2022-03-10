databaseChangeLog = {

    changeSet(author: "kloberd (generated)", id: "1623760508083-1") {
        dropColumn( columnName: "lic_type_rv_fk", tableName: "license" )
    }
}
