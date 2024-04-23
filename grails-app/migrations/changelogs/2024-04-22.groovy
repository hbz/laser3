package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1713471812092-1") {
        createTable(tableName: "click_me_config") {
            column(autoIncrement: "true", name: "cmc_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "click_me_configPK")
            }

            column(name: "cmc_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "cmc_context_url", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "cmc_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "cmc_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "cmc_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "cmc_name_of_click_me_map", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "cmc_click_me_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "cmc_json_config", type: "TEXT") {
                constraints(nullable: "false")
            }

            column(name: "cmc_context_org_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }


    changeSet(author: "djebeniani (generated)", id: "1713471812092-2") {
        createIndex(indexName: "cmc_context_org_idx", tableName: "click_me_config") {
            column(name: "cmc_context_org_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1713471812092-3") {
        addForeignKeyConstraint(baseColumnNames: "cmc_context_org_fk", baseTableName: "click_me_config", constraintName: "FKnfk2l41fp6i39ipc8w3mp453i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1713471812092-4") {
        addColumn(tableName: "click_me_config") {
            column(name: "cmc_config_order", type: "int4")
        }
    }

}
