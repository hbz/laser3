databaseChangeLog = {

    changeSet(author: "galffy (modified)", id: "1619422166955-1") {
        addColumn(tableName: "cost_item") {
            column(name: "ci_billing_sum_rounding", type: "boolean")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1619422166955-2") {
        grailsChange {
            change {
                sql.execute('update cost_item set ci_billing_sum_rounding = false;')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1619422166955-3") {
        addDefaultValue(columnDataType: "boolean", columnName: "ci_billing_sum_rounding", defaultValueBoolean: false, tableName: "cost_item")
    }

    changeSet(author: "galffy (modified)", id: "1619422166955-4") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "ci_billing_sum_rounding", tableName: "cost_item")
    }

}
