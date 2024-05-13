package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1715584072160-1") {
        grailsChange {
            change {
                String query = "delete from property_definition where pd_name = 'NatStat Supplier ID'"
                sql.execute(query)
                int deleted = sql.getUpdateCount()
                confirm("${query}: ${deleted}")
                changeSet.setComments("${query}: ${deleted}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1715584072160-2") {
        addColumn(tableName: "alternative_name") {
            column(name: "altname_lic_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1715584072160-3") {
        addColumn(tableName: "alternative_name") {
            column(name: "altname_sub_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1715584072160-4") {
        addForeignKeyConstraint(baseColumnNames: "altname_sub_fk", baseTableName: "alternative_name", constraintName: "FK2oejcybkxjlsj63ommi1urleo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1715584072160-5") {
        addForeignKeyConstraint(baseColumnNames: "altname_lic_fk", baseTableName: "alternative_name", constraintName: "FKssa5eqadwoawymqr7b090efva", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1715584072160-6") {
        addColumn(tableName: "property_definition_group") {
            column(name: "pdg_order", type: "int8")
        }
    }

}
