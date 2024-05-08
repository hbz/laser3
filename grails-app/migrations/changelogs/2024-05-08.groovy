package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1715152972155-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "eb_vendor_fk", tableName: "electronic_billing")
    }

    changeSet(author: "galffy (generated)", id: "1715152972155-2") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "idi_vendor_fk", tableName: "invoice_dispatch")
    }

}
