package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1715752295721-1") {
        createTable(tableName: "provider_link") {
            column(autoIncrement: "true", name: "pl_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "provider_linkPK")
            }

            column(name: "pl_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pl_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "pl_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "pl_type_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pl_from_prov_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pl_to_prov_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1715752295721-2") {
        createTable(tableName: "vendor_link") {
            column(autoIncrement: "true", name: "vl_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "vendor_linkPK")
            }

            column(name: "vl_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "vl_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "vl_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "vl_type_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "vl_from_ven_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "vl_to_ven_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1715752295721-3") {
        addForeignKeyConstraint(baseColumnNames: "vl_from_ven_fk", baseTableName: "vendor_link", constraintName: "FK3rb1irqxbpq66gv6fv7v8due", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1715752295721-4") {
        addForeignKeyConstraint(baseColumnNames: "pl_from_prov_fk", baseTableName: "provider_link", constraintName: "FK3ucl82gb0x1mbs1pjimudvmxx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1715752295721-5") {
        addForeignKeyConstraint(baseColumnNames: "pl_type_rv_fk", baseTableName: "provider_link", constraintName: "FKcmk2fru7h0nj7dia2b5f2r14o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1715752295721-6") {
        addForeignKeyConstraint(baseColumnNames: "vl_to_ven_fk", baseTableName: "vendor_link", constraintName: "FKgoldae2t78kfmm5l52s0dnorg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1715752295721-7") {
        addForeignKeyConstraint(baseColumnNames: "vl_type_rv_fk", baseTableName: "vendor_link", constraintName: "FKrdalv1hgjagn0c4u5b37t1i7r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1715752295721-8") {
        addForeignKeyConstraint(baseColumnNames: "pl_to_prov_fk", baseTableName: "provider_link", constraintName: "FKxxqv6icjvyos3rs80yh1l398", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1715752295721-9") {
        addColumn(tableName: "identifier") {
            column(name: "id_vendor_fk", type: "int8")
        }
    }
    
    changeSet(author: "galffy (generated)", id: "1715752295721-10") {
        createIndex(indexName: "id_vendor_idx", tableName: "identifier") {
            column(name: "id_vendor_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1715752295721-11") {
        addForeignKeyConstraint(baseColumnNames: "id_vendor_fk", baseTableName: "identifier", constraintName: "FK1tgq7ydyacj2nvnfdh6b55kj2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1715752295721-12") {
        addColumn(tableName: "wf_checklist") {
            column(name: "wfcl_prov_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1715752295721-13") {
        addColumn(tableName: "wf_checklist") {
            column(name: "wfcl_ven_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1715752295721-14") {
        addForeignKeyConstraint(baseColumnNames: "wfcl_prov_fk", baseTableName: "wf_checklist", constraintName: "FKag6hpatcag75w69vqbityvyv2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1715752295721-15") {
        addForeignKeyConstraint(baseColumnNames: "wfcl_ven_fk", baseTableName: "wf_checklist", constraintName: "FKepxrk66hh169rga5isx5cfut0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }
}
