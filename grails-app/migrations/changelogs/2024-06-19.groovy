package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1718792125385-1") {
        grailsChange {
            change {
                String query = "DELETE FROM contact WHERE ct_org_fk is not null or ct_provider_fk is not null or ct_vendor_fk is not null"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1718792125385-2") {
        dropForeignKeyConstraint(baseTableName: "contact", constraintName: "FKjeayne70tjjc396rfl8lplcbi")
    }

    changeSet(author: "galffy (generated)", id: "1718792125385-3") {
        dropForeignKeyConstraint(baseTableName: "contact", constraintName: "FKo2bsn6k0tah9k83lmhl2pvs3d")
    }

    changeSet(author: "galffy (generated)", id: "1718792125385-4") {
        dropForeignKeyConstraint(baseTableName: "contact", constraintName: "fk38b724202bc428d5")
    }

    changeSet(author: "galffy (generated)", id: "1718792125385-5") {
        dropColumn(columnName: "ct_org_fk", tableName: "contact")
    }

    changeSet(author: "galffy (generated)", id: "1718792125385-6") {
        dropColumn(columnName: "ct_provider_fk", tableName: "contact")
    }

    changeSet(author: "galffy (generated)", id: "1718792125385-7") {
        dropColumn(columnName: "ct_vendor_fk", tableName: "contact")
    }
}
