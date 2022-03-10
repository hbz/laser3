databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1626172637921-1") {
        addColumn(tableName: "customer_identifier") {
            column(name: "cid_requestor_key", type: "text")
        }
    }

    changeSet(author: "galffy (generated)", id: "1626172637921-2") {
        dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "cid_value", tableName: "customer_identifier")
    }

}