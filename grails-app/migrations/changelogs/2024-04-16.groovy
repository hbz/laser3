package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1713268353429-1") {
        dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "ven_gokb_id", tableName: "vendor")
    }
}
