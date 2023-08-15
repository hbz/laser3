package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1691652087822-1") {
        createTable(tableName: "marker") {
            column(autoIncrement: "true", name: "mkr_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "markerPK")
            }

            column(name: "mkr_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "mkr_org_fk", type: "BIGINT")

            column(name: "mkr_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "mkr_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "mkr_user_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "mkr_type_enum", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "mkr_plt_fk", type: "BIGINT")

            column(name: "mkr_pkg_fk", type: "BIGINT")
        }
    }

    changeSet(author: "klober (generated)", id: "1691652087822-2") {
        createIndex(indexName: "mkr_user_idx", tableName: "marker") {
            column(name: "mkr_user_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1691652087822-3") {
        addForeignKeyConstraint(baseColumnNames: "mkr_org_fk", baseTableName: "marker", constraintName: "FK7kccnoipbex3kqa0hlyvkd6sr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1691652087822-4") {
        addForeignKeyConstraint(baseColumnNames: "mkr_pkg_fk", baseTableName: "marker", constraintName: "FKcwxcq7icwglj637k3t7yn0ak4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pkg_id", referencedTableName: "package", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1691652087822-5") {
        addForeignKeyConstraint(baseColumnNames: "mkr_plt_fk", baseTableName: "marker", constraintName: "FKllf2y5x73ff8n8fdtjx06777k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1691652087822-6") {
        addForeignKeyConstraint(baseColumnNames: "mkr_user_fk", baseTableName: "marker", constraintName: "FKmdoac25m9fg8u85uo9cdg7p6q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "usr_id", referencedTableName: "user", validate: "true")
    }
}
