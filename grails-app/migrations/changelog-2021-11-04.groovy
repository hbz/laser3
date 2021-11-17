databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1636025428570-1") {
        createTable(tableName: "reporting_filter") {
            column(autoIncrement: "true", name: "rf_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "reporting_filterPK")
            }

            column(name: "rf_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "rf_filter_map", type: "TEXT") {
                constraints(nullable: "false")
            }

            column(name: "rf_token", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "rf_title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "rf_labels", type: "TEXT")

            column(name: "rf_date_created", type: "TIMESTAMP")

            column(name: "rf_filter", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "rf_last_updated", type: "TIMESTAMP")

            column(name: "rf_owner_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "rf_description", type: "TEXT")
        }
    }

    changeSet(author: "klober (generated)", id: "1636025428570-2") {
        addForeignKeyConstraint(baseColumnNames: "rf_owner_fk", baseTableName: "reporting_filter", constraintName: "FKthh0jxunwdb5fc5o4xf6pcbfs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user")
    }
}
