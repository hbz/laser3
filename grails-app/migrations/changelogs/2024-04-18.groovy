package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1713447202534-1") {
        createTable(tableName: "vendor_property") {
            column(autoIncrement: "true", name: "vp_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "vendor_propertyPK")
            }

            column(name: "vp_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "vp_ref_value_rv_fk", type: "BIGINT")

            column(name: "vp_int_value", type: "INTEGER")

            column(name: "vp_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "vp_date_value", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "vp_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "vp_dec_value", type: "numeric(19, 2)")

            column(name: "vp_owner_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "vp_is_public", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "vp_tenant_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "vp_string_value", type: "TEXT")

            column(name: "vp_last_updated_cascading", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "vp_type_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "vp_note", type: "TEXT")

            column(name: "vp_url_value", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1713447202534-2") {
        createIndex(indexName: "vp_owner_idx", tableName: "vendor_property") {
            column(name: "vp_owner_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1713447202534-3") {
        createIndex(indexName: "vp_tenant_idx", tableName: "vendor_property") {
            column(name: "vp_tenant_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1713447202534-4") {
        createIndex(indexName: "vp_type_idx", tableName: "vendor_property") {
            column(name: "vp_type_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1713447202534-5") {
        addForeignKeyConstraint(baseColumnNames: "vp_owner_fk", baseTableName: "vendor_property", constraintName: "FKdnxbu9ksjcx43v6wbda9w4fxx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1713447202534-6") {
        addForeignKeyConstraint(baseColumnNames: "vp_type_fk", baseTableName: "vendor_property", constraintName: "FKg14dg562q60v43wgf9vqt469l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1713447202534-7") {
        addForeignKeyConstraint(baseColumnNames: "vp_tenant_fk", baseTableName: "vendor_property", constraintName: "FKgyvckbcb0nokka18k1tguws9c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1713447202534-8") {
        addForeignKeyConstraint(baseColumnNames: "vp_ref_value_rv_fk", baseTableName: "vendor_property", constraintName: "FKtkyb5yucg04wt0m80xmiw18i4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (hand-coded)", id: "1713447202534-9") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE vendor ALTER COLUMN ven_homepage TYPE character varying(512);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1713447202534-10") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE vendor ALTER COLUMN ven_prequalification_vol_info TYPE text;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1713447202534-11") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE vendor ALTER COLUMN ven_research_platform_ebooks TYPE text;")
            }
            rollback {}
        }
    }
}
