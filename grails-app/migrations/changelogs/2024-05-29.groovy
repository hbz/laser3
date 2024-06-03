package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1716986919077-1") {
        addColumn(tableName: "provider") {
            column(name: "prov_legally_obliged_by_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1716986919077-2") {
        addColumn(tableName: "vendor") {
            column(name: "ven_legally_obliged_by_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1716986919077-3") {
        addForeignKeyConstraint(baseColumnNames: "ven_legally_obliged_by_fk", baseTableName: "vendor", constraintName: "FK3axe8ky1n92089er62wsay4lw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1716986919077-4") {
        addForeignKeyConstraint(baseColumnNames: "prov_legally_obliged_by_fk", baseTableName: "provider", constraintName: "FKjiv836qft92fl2gtnoviqfv9b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1716986919077-5") {
        addColumn(tableName: "provider") {
            column(name: "prov_created_by_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1716986919077-6") {
        addColumn(tableName: "vendor") {
            column(name: "ven_created_by_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1716986919077-7") {
        addForeignKeyConstraint(baseColumnNames: "prov_created_by_fk", baseTableName: "provider", constraintName: "FK6ubfg9iaocfx6s63wetck9whw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1716986919077-8") {
        addForeignKeyConstraint(baseColumnNames: "ven_created_by_fk", baseTableName: "vendor", constraintName: "FKeleq518944q7sja61o7bd8v7p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }
}
