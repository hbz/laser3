package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1715082600851-1") {
        addColumn(tableName: "marker") {
            column(name: "mkr_tipp_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1715082600851-2") {
        addForeignKeyConstraint(baseColumnNames: "mkr_tipp_fk", baseTableName: "marker", constraintName: "FK1q0n626gpehsx9hmip7xnmlya", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform", validate: "true")
    }

}
