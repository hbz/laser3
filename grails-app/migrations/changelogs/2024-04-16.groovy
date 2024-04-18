package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1713268353429-1") {
        dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "ven_gokb_id", tableName: "vendor")
    }

    changeSet(author: "klober (modified)", id: "1713268353429-2") {
        grailsChange {
            change {
                sql.executeUpdate("delete from system_event where se_token = 'DBDD_SERVICE_START_1'")
                sql.executeUpdate("delete from system_event where se_token = 'DBDD_SERVICE_COMPLETE_1'")
            }
            rollback {}
        }
    }
}
