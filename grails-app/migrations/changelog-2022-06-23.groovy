databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1655994622356-1") {
        createTable(tableName: "survey_links") {
            column(autoIncrement: "true", name: "surlin_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "survey_linksPK")
            }

            column(name: "surlin_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surlin_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surlin_target_survey", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surlin_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surlin_source_survey", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1655994622356-2") {
        addForeignKeyConstraint(baseColumnNames: "surlin_source_survey", baseTableName: "survey_links", constraintName: "FKoduq8qwcl0b0ei7ad7npoa37q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surin_id", referencedTableName: "survey_info")
    }

    changeSet(author: "djebeniani (generated)", id: "1655994622356-3") {
        addForeignKeyConstraint(baseColumnNames: "surlin_target_survey", baseTableName: "survey_links", constraintName: "FKq7cugthyvakvjmwaqdt60da0t", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surin_id", referencedTableName: "survey_info")
    }
}
