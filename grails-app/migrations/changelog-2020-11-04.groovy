databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1604472510001-1") {
        addColumn(tableName: "user") {
            column(name: "image", type: "varchar(255)")
        }
    }

//    changeSet(author: "klober (generated)", id: "1604472510001-2") {
//        addNotNullConstraint(columnDataType: "bigint", columnName: "lp_tenant_fk", tableName: "license_property")
//    }
//
//    changeSet(author: "klober (generated)", id: "1604472510001-3") {
//        addNotNullConstraint(columnDataType: "bigint", columnName: "pp_tenant_fk", tableName: "person_property")
//    }
//
//    changeSet(author: "klober (generated)", id: "1604472510001-4") {
//        addNotNullConstraint(columnDataType: "bigint", columnName: "sp_tenant_fk", tableName: "subscription_property")
//    }
}
