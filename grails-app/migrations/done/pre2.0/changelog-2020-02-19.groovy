databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1582099914868-1") {
        addColumn(schemaName: "public", tableName: "subscription") {
            column(name: "sub_has_perpetual_access", type: "bool")
        }
    }

    changeSet(author: "galffy (modified)", id: "1582099914868-2") {
        grailsChange {
            change {
                sql.execute("update subscription set sub_has_perpetual_access=false where sub_has_perpetual_access is null;")
            }
            rollback {
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1582099914868-3") {
        addNotNullConstraint(columnDataType: "bool", columnName: "sub_has_perpetual_access", tableName: "subscription")
    }
}