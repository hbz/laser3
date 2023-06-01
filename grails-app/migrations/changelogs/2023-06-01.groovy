databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1685618338883-1") {
        dropColumn(columnName: "sp_freeze_holding", tableName: "subscription_package")
    }

}
