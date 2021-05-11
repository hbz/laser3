databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1620713794091-1") {
        createTable(tableName: "language") {
            column(autoIncrement: "true", name: "lang_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "languagePK")
            }

            column(name: "lang_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "lang_tipp_fk", type: "BIGINT")

            column(name: "lang_date_created", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "lang_last_updated_cascading", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "lang_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "lang_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "lang_pkg_fk", type: "BIGINT")
        }
    }

    changeSet(author: "galffy (generated)", id: "1620713794091-2") {
        addForeignKeyConstraint(baseColumnNames: "lang_pkg_fk", baseTableName: "language", constraintName: "FK4ex6ksv5tbw93tnmq3i6772ip", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pkg_id", referencedTableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1620713794091-3") {
        addForeignKeyConstraint(baseColumnNames: "lang_tipp_fk", baseTableName: "language", constraintName: "FK9gy5oqcw8wy5nrylrb6rcmv4w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1620713794091-4") {
        addForeignKeyConstraint(baseColumnNames: "lang_rv_fk", baseTableName: "language", constraintName: "FKl3g3hk4bm63y9bglnplomoawx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

}
