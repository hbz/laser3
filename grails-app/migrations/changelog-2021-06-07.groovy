databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1623049813501-1") {
        addColumn(tableName: "org") {
            column(name: "org_retirement_date", type: "timestamp")
        }
    }

    changeSet(author: "galffy (modified)", id: "1623049813501-2") {
        addColumn(tableName: "subscription_package") {
            column(name: "sp_freeze_holding", type: "boolean")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623049813501-3") {
        grailsChange {
            change {
                sql.execute('update subscription_package set sp_freeze_holding = false;')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623049813501-4") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "sp_freeze_holding", tableName: "subscription_package")
    }

}
