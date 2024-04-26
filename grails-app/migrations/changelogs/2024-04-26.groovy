package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1714117723136-1") {
        createTable(tableName: "sushi_call_error") {
            column(autoIncrement: "true", name: "sce_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "sushi_call_errorPK")
            }

            column(name: "sce_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sce_org_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sce_err_mess", type: "VARCHAR(255)")

            column(name: "sce_platform_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sce_requestor_id", type: "VARCHAR(255)")

            column(name: "sce_customer_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1714117723136-2") {
        addForeignKeyConstraint(baseColumnNames: "sce_org_fk", baseTableName: "sushi_call_error", constraintName: "FK9r4plw01rei1l3ggrme11ty4i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714117723136-3") {
        addForeignKeyConstraint(baseColumnNames: "sce_platform_fk", baseTableName: "sushi_call_error", constraintName: "FKqssjx02pqddk1i30h503kfbbi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform", validate: "true")
    }
}
