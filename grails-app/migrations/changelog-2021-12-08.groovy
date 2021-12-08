databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1638961350070-1") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_access_type_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1638961350070-2") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_open_access_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1638961350070-3") {
        addForeignKeyConstraint(baseColumnNames: "tipp_access_type_rv_fk", baseTableName: "title_instance_package_platform", constraintName: "FKcwv4chprfb1f7vsdi6sbv2t49", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "galffy (generated)", id: "1638961350070-4") {
        addForeignKeyConstraint(baseColumnNames: "tipp_open_access_rv_fk", baseTableName: "title_instance_package_platform", constraintName: "FKkcfxhr2ip15bs2cnui2s9xq4u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

}
