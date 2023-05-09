package changelogs


databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1683531152301-1") {
        createTable(tableName: "permanent_title") {
            column(autoIncrement: "true", name: "pt_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "permanent_titlePK")
            }

            column(name: "pt_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pt_ie_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pt_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "pt_subscription_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pt_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "pt_tipp_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pt_owner_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1683531152301-2") {
        addForeignKeyConstraint(baseColumnNames: "pt_ie_fk", baseTableName: "permanent_title", constraintName: "FKdgopfa6r885u5oy3l9k9pm2j6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ie_id", referencedTableName: "issue_entitlement", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1683531152301-3") {
        addForeignKeyConstraint(baseColumnNames: "pt_owner_fk", baseTableName: "permanent_title", constraintName: "FKdoi4tn2w59w3wcb6imbn98re9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1683531152301-4") {
        addForeignKeyConstraint(baseColumnNames: "pt_subscription_fk", baseTableName: "permanent_title", constraintName: "FKlmxe0wj71g489qo3rpi1lrvr9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1683531152301-5") {
        addForeignKeyConstraint(baseColumnNames: "pt_tipp_fk", baseTableName: "permanent_title", constraintName: "FKsalhkgmmlmq4v0lmy9sgdcfsf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform", validate: "true")
    }


}
