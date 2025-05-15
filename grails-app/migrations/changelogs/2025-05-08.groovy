package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1746701938379-1") {
        createTable(tableName: "survey_transfer") {
            column(autoIncrement: "true", name: "surtrans_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_transferPK")
            }

            column(name: "surtrans_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surtrans_org_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surtrans_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surtrans_subscription_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surtrans_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surtrans_transfer_date", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surtrans_surveyconfig_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746701938379-2") {
        createIndex(indexName: "surtrans_org_idx", tableName: "survey_transfer") {
            column(name: "surtrans_org_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746701938379-3") {
        createIndex(indexName: "surtrans_subscription_idx", tableName: "survey_transfer") {
            column(name: "surtrans_subscription_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746701938379-4") {
        createIndex(indexName: "surtrans_surveyconfig_idx", tableName: "survey_transfer") {
            column(name: "surtrans_surveyconfig_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746701938379-5") {
        addForeignKeyConstraint(baseColumnNames: "surtrans_surveyconfig_fk", baseTableName: "survey_transfer", constraintName: "FK5e9rol9fxdxib6dpxg5t0ndhu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1746701938379-6") {
        addForeignKeyConstraint(baseColumnNames: "surtrans_org_fk", baseTableName: "survey_transfer", constraintName: "FKgch8c041dhhwqdmow6aude4o8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1746701938379-7") {
        addForeignKeyConstraint(baseColumnNames: "surtrans_subscription_fk", baseTableName: "survey_transfer", constraintName: "FKgl47ti4ppfu084eegg0x5cfxt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", validate: "true")
    }

}
