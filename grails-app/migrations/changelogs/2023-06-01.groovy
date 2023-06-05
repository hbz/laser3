databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1685618338883-1") {
        dropColumn(columnName: "sp_freeze_holding", tableName: "subscription_package")
    }

    changeSet(author: "galffy (hand-coded)", id: "1685618338883-2") {
        grailsChange {
            change {
                sql.executeUpdate("delete from audit_config where auc_reference_field = 'freezeHolding'")
            }
            rollback {}
        }
    }
}
