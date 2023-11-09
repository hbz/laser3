package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1699424300712-1") {
        createTable(tableName: "discovery_system_frontend") {
            column(autoIncrement: "true", name: "dsf_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "discovery_system_frontendPK")
            }

            column(name: "dsf_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "dsf_frontend_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "dsf_org_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "dsf_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "dsf_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1699424300712-2") {
        createTable(tableName: "discovery_system_index") {
            column(autoIncrement: "true", name: "dsi_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "discovery_system_indexPK")
            }

            column(name: "dsi_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "dsi_index_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "dsi_org_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "dsi_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "dsi_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1699424300712-3") {
        createIndex(indexName: "dsf_frontend_idx", tableName: "discovery_system_frontend") {
            column(name: "dsf_frontend_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1699424300712-4") {
        createIndex(indexName: "dsf_org_idx", tableName: "discovery_system_frontend") {
            column(name: "dsf_org_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1699424300712-5") {
        createIndex(indexName: "dsi_index_idx", tableName: "discovery_system_index") {
            column(name: "dsi_index_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1699424300712-6") {
        createIndex(indexName: "dsi_org_idx", tableName: "discovery_system_index") {
            column(name: "dsi_org_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1699424300712-7") {
        addForeignKeyConstraint(baseColumnNames: "dsf_org_fk", baseTableName: "discovery_system_frontend", constraintName: "FK8xfbr96qn46yv5d7igxq9jca3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1699424300712-8") {
        addForeignKeyConstraint(baseColumnNames: "dsi_org_fk", baseTableName: "discovery_system_index", constraintName: "FKikm5nbiauwkgaofhab0xmxa67", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1699424300712-9") {
        addForeignKeyConstraint(baseColumnNames: "dsi_index_rv_fk", baseTableName: "discovery_system_index", constraintName: "FKkj4rx7s4yutylphxfxd6mqro3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1699424300712-10") {
        addForeignKeyConstraint(baseColumnNames: "dsf_frontend_rv_fk", baseTableName: "discovery_system_frontend", constraintName: "FKn27y2g6hem9go5cv700easuga", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }
}
