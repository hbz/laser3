package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1742206155038-1") {
        createTable(tableName: "cost_information_definition") {
            column(autoIncrement: "true", name: "cif_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "cost_information_definitionPK")
            }

            column(name: "cif_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "cif_explanation_de", type: "TEXT")

            column(name: "cif_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "cif_is_hard_data", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "cif_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "cif_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "cif_name_en", type: "VARCHAR(255)")

            column(name: "cif_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "refdata_category", type: "VARCHAR(255)")

            column(name: "cif_name_de", type: "VARCHAR(255)")

            column(name: "cif_tenant_fk", type: "BIGINT")

            column(name: "cif_explanation_en", type: "TEXT")
        }
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-2") {
        createTable(tableName: "cost_information_definition_group") {
            column(autoIncrement: "true", name: "cifg_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "cost_information_definition_groupPK")
            }

            column(name: "cifg_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "cifg_cost_information_definition_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "cifg_tenant_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-3") {
        addColumn(tableName: "cost_item") {
            column(name: "ci_cost_information_definition_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-4") {
        addColumn(tableName: "cost_item") {
            column(name: "ci_cost_information_ref_value_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-5") {
        addColumn(tableName: "cost_item") {
            column(name: "ci_cost_information_string_value", type: "text")
        }
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-6") {
        createIndex(indexName: "ci_cost_information_definition_idx", tableName: "cost_item") {
            column(name: "ci_cost_information_definition_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-7") {
        createIndex(indexName: "ci_cost_information_ref_value_idx", tableName: "cost_item") {
            column(name: "ci_cost_information_ref_value_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-8") {
        createIndex(indexName: "cif_name_idx", tableName: "cost_information_definition") {
            column(name: "cif_name")
        }
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-9") {
        createIndex(indexName: "cif_tenant_idx", tableName: "cost_information_definition") {
            column(name: "cif_tenant_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-10") {
        createIndex(indexName: "cif_type_idx", tableName: "cost_information_definition") {
            column(name: "cif_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-11") {
        createIndex(indexName: "cifg_cost_information_definition_idx", tableName: "cost_information_definition_group") {
            column(name: "cifg_cost_information_definition_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-12") {
        createIndex(indexName: "cifg_tenant_idx", tableName: "cost_information_definition_group") {
            column(name: "cifg_tenant_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-13") {
        addForeignKeyConstraint(baseColumnNames: "cif_tenant_fk", baseTableName: "cost_information_definition", constraintName: "FKatb6o5sue20p06hsnwlw8tttw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-14") {
        addForeignKeyConstraint(baseColumnNames: "cifg_tenant_fk", baseTableName: "cost_information_definition_group", constraintName: "FKg2oiihf1ko302v1oolkcld5p4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-15") {
        addForeignKeyConstraint(baseColumnNames: "ci_cost_information_ref_value_rv_fk", baseTableName: "cost_item", constraintName: "FKi56p6b4ck3sihfwhbrvyjjc70", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-16") {
        addForeignKeyConstraint(baseColumnNames: "ci_cost_information_definition_fk", baseTableName: "cost_item", constraintName: "FKlw7yk3fupuydf6s8cvoawccgy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "cif_id", referencedTableName: "cost_information_definition", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1742206155038-17") {
        addForeignKeyConstraint(baseColumnNames: "cifg_cost_information_definition_fk", baseTableName: "cost_information_definition_group", constraintName: "FKph4nk5cueuswu3e5htkiya4fe", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "cif_id", referencedTableName: "cost_information_definition", validate: "true")
    }
}
