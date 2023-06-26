databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1687772266004-1") {
        dropForeignKeyConstraint(baseTableName: "issue_entitlement", constraintName: "FKcajcx0oxg88wk5a2rrhgnoqi2")
    }

    changeSet(author: "galffy (generated)", id: "1687772266004-2") {
        dropForeignKeyConstraint(baseTableName: "issue_entitlement", constraintName: "FKl45p65k7799i7sog0s9nkvr0t")
    }

    changeSet(author: "galffy (generated)", id: "1687772266004-3") {
        dropForeignKeyConstraint(baseTableName: "issue_entitlement", constraintName: "fk2d45f6c75bea734a")
    }

    changeSet(author: "galffy (generated)", id: "1687772266004-4") {
        dropColumn(columnName: "ie_access_type_rv_fk", tableName: "issue_entitlement")
    }

    changeSet(author: "galffy (generated)", id: "1687772266004-5") {
        dropColumn(columnName: "ie_medium_rv_fk", tableName: "issue_entitlement")
    }

    changeSet(author: "galffy (generated)", id: "1687772266004-6") {
        dropColumn(columnName: "ie_name", tableName: "issue_entitlement")
    }

    changeSet(author: "galffy (generated)", id: "1687772266004-7") {
        dropColumn(columnName: "ie_open_access_rv_fk", tableName: "issue_entitlement")
    }

    changeSet(author: "galffy (generated)", id: "1687772266004-8") {
        dropColumn(columnName: "ie_sortname", tableName: "issue_entitlement")
    }
}
