package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1712585250329-1") {
        createTable(tableName: "package_vendor") {
            column(autoIncrement: "true", name: "pv_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "package_vendorPK")
            }

            column(name: "pv_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pv_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "pv_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "pv_vendor_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pv_pkg_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-2") {
        createTable(tableName: "vendor") {
            column(autoIncrement: "true", name: "ven_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "vendorPK")
            }

            column(name: "ven_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "ven_gokb_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "ven_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "ven_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "ven_sortname", type: "VARCHAR(255)")

            column(name: "ven_last_updated_cascading", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "ven_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "ven_guid", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "ven_status_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-3") {
        createTable(tableName: "vendor_role") {
            column(autoIncrement: "true", name: "vr_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "vendor_rolePK")
            }

            column(name: "vr_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "vr_is_shared", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "vr_shared_from_fk", type: "BIGINT")

            column(name: "vr_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "vr_subscription_fk", type: "BIGINT")

            column(name: "vr_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "vr_vendor_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "vr_license_fk", type: "BIGINT")
        }
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-4") {
        addColumn(tableName: "address") {
            column(name: "adr_vendor_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-5") {
        addColumn(tableName: "contact") {
            column(name: "ct_vendor_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-6") {
        addColumn(tableName: "marker") {
            column(name: "mkr_ven_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-7") {
        addColumn(tableName: "person_role") {
            column(name: "pr_vendor_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-8") {
        addUniqueConstraint(columnNames: "ven_gokb_id", constraintName: "UC_VENDORVEN_GOKB_ID_COL", tableName: "vendor")
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-9") {
        addUniqueConstraint(columnNames: "ven_guid", constraintName: "UC_VENDORVEN_GUID_COL", tableName: "vendor")
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-10") {
        createIndex(indexName: "adr_vendor_idx", tableName: "address") {
            column(name: "adr_vendor_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-11") {
        createIndex(indexName: "ct_vendor_idx", tableName: "contact") {
            column(name: "ct_vendor_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-12") {
        createIndex(indexName: "pr_prs_ven_idx", tableName: "person_role") {
            column(name: "pr_vendor_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-13") {
        createIndex(indexName: "vr_license_idx", tableName: "vendor_role") {
            column(name: "vr_license_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-14") {
        createIndex(indexName: "vr_subscription_idx", tableName: "vendor_role") {
            column(name: "vr_subscription_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-15") {
        createIndex(indexName: "vr_vendor_idx", tableName: "vendor_role") {
            column(name: "vr_vendor_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-16") {
        addForeignKeyConstraint(baseColumnNames: "vr_subscription_fk", baseTableName: "vendor_role", constraintName: "FK16js3o7wghjdgjcggclm65ugf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-17") {
        addForeignKeyConstraint(baseColumnNames: "vr_shared_from_fk", baseTableName: "vendor_role", constraintName: "FK72ifemvjpmnfx37npm8q328l4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "vr_id", referencedTableName: "vendor_role", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-18") {
        addForeignKeyConstraint(baseColumnNames: "pv_vendor_fk", baseTableName: "package_vendor", constraintName: "FK84qfewt22i3txkteckf92hhfq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-19") {
        addForeignKeyConstraint(baseColumnNames: "ven_status_rv_fk", baseTableName: "vendor", constraintName: "FK8hbhyd1tyurd2d4tyipqrxrdp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-20") {
        addForeignKeyConstraint(baseColumnNames: "vr_vendor_fk", baseTableName: "vendor_role", constraintName: "FK9ip630kg9jgriv9mfbdqwftqi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-21") {
        addForeignKeyConstraint(baseColumnNames: "pv_pkg_fk", baseTableName: "package_vendor", constraintName: "FKcqvcd2n3krb1ey80fhvflh66a", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pkg_id", referencedTableName: "package", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-22") {
        addForeignKeyConstraint(baseColumnNames: "vr_license_fk", baseTableName: "vendor_role", constraintName: "FKcsf261mqaegyo3ui1l5s6av5s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-23") {
        addForeignKeyConstraint(baseColumnNames: "pr_vendor_fk", baseTableName: "person_role", constraintName: "FKdlh996ws2w6tq1ccbwfk7glr7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-24") {
        addForeignKeyConstraint(baseColumnNames: "ct_vendor_fk", baseTableName: "contact", constraintName: "FKjeayne70tjjc396rfl8lplcbi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-25") {
        addForeignKeyConstraint(baseColumnNames: "adr_vendor_fk", baseTableName: "address", constraintName: "FKq99grs7xtarjans5vmtrs7aq4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712585250329-26") {
        addForeignKeyConstraint(baseColumnNames: "mkr_ven_fk", baseTableName: "marker", constraintName: "FKshdhkxqgp1hvmu0ydwricsecs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }
}
