package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1653297937420-1") {
        dropForeignKeyConstraint(baseTableName: "cost_item", constraintName: "fkefe45c45b30b076b")
    }

    changeSet(author: "galffy (generated)", id: "1653297937420-2") {
        dropColumn(columnName: "ci_tax_code", tableName: "cost_item")
    }

    changeSet(author: "galffy (generated)", id: "1653297937420-3") {
        dropColumn(columnName: "ci_tax_rate", tableName: "cost_item")
    }

    changeSet(author: "galffy (hand-coded)", id: "1653297937420-4") {
        grailsChange {
            change {
                sql.execute("delete from refdata_value where rdv_is_hard_data = false and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'tax.type')")
            }
            rollback {}
        }
    }

}
