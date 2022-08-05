databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1659682856959-1") {
        addColumn(tableName: "system_event") {
            column(name: "se_has_changed", type: "boolean")
        }
    }

    changeSet(author: "klober (modified)", id: "1659682856959-2") {
        grailsChange {
            change {
                sql.execute("update system_event set se_has_changed = false where se_has_changed is null")

            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1659682856959-3") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "se_has_changed", tableName: "system_event", validate: "true")
    }
}