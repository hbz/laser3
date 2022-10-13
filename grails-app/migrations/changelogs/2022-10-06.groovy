databaseChangeLog = {

    changeSet(author: "galffy (modified)", id: "1665032822993-1") {
        addColumn(tableName: "cost_item_element_configuration") {
            column(name: "ciec_use_for_cost_per_use", type: "boolean")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1665032822993-2") {
        grailsChange {
            change {
                sql.execute('update cost_item_element_configuration set ciec_use_for_cost_per_use = false')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1665032822993-3") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "ciec_use_for_cost_per_use", tableName: "cost_item_element_configuration", validate: "true")
    }

}
