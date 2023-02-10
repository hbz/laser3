package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1674565457733-1") {
        createTable(tableName: "wf_checklist") {
            column(autoIncrement: "true", name: "wfcl_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "wf_checklistPK")
            }

            column(name: "wfcl_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfcl_org_fk", type: "BIGINT")

            column(name: "wfcl_is_template", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "wfcl_date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfcl_last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfcl_owner_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfcl_license_fk", type: "BIGINT")

            column(name: "wfcl_title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "wfcl_comment", type: "TEXT")

            column(name: "wfcl_subscription_fk", type: "BIGINT")

            column(name: "wfcl_description", type: "TEXT")
        }
    }

    changeSet(author: "klober (generated)", id: "1674565457733-2") {
        createTable(tableName: "wf_checkpoint") {
            column(autoIncrement: "true", name: "wfcp_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "wf_checkpointPK")
            }

            column(name: "wfcp_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_position", type: "INTEGER") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_is_done", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_comment", type: "TEXT")

            column(name: "wfcp_date", type: "TIMESTAMP")

            column(name: "wfcp_date_created", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_last_updated", type: "TIMESTAMP") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_checklist_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "wfcp_description", type: "TEXT")
        }
    }

    changeSet(author: "klober (generated)", id: "1674565457733-3") {
        addForeignKeyConstraint(baseColumnNames: "wfcl_org_fk", baseTableName: "wf_checklist", constraintName: "FK708k6xoefj98e4kji1vsn8hka", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1674565457733-4") {
        addForeignKeyConstraint(baseColumnNames: "wfcl_license_fk", baseTableName: "wf_checklist", constraintName: "FK9osi0l7maatven2457xkm4hx7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1674565457733-5") {
        addForeignKeyConstraint(baseColumnNames: "wfcl_subscription_fk", baseTableName: "wf_checklist", constraintName: "FKb7qrif6c1hq0rrojgf49hcwwf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1674565457733-6") {
        addForeignKeyConstraint(baseColumnNames: "wfcp_checklist_fk", baseTableName: "wf_checkpoint", constraintName: "FKgojm5oab69gysfxekcafeufid", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "wfcl_id", referencedTableName: "wf_checklist", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1674565457733-7") {
        addForeignKeyConstraint(baseColumnNames: "wfcl_owner_fk", baseTableName: "wf_checklist", constraintName: "FKgqdo9lymr3u72ss1rh4oqq1im", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }
}
