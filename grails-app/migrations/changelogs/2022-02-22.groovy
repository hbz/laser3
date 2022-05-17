package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1645517965908-1") {
        addColumn(tableName: "stats_missing_period") {
            column(name: "smp_customer_fk", type: "int8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1645517965908-2") {
        addColumn(tableName: "stats_missing_period") {
            column(name: "smp_platform_fk", type: "int8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1645517965908-3") {
        addColumn(tableName: "stats_missing_period") {
            column(name: "smp_report_id", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1645517965908-4") {
        addUniqueConstraint(columnNames: "smp_platform_fk, smp_customer_fk, smp_to_date, smp_from_date, smp_report_id", constraintName: "UK2dacbafa0fdadc310145de4082d5", tableName: "stats_missing_period")
    }

    changeSet(author: "galffy (generated)", id: "1645517965908-5") {
        createIndex(indexName: "smp_report_idx", tableName: "stats_missing_period") {
            column(name: "smp_report_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1645517965908-6") {
        addForeignKeyConstraint(baseColumnNames: "smp_customer_fk", baseTableName: "stats_missing_period", constraintName: "FKk38glmww15w34iqa4qef3qr3h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org")
    }

    changeSet(author: "galffy (generated)", id: "1645517965908-7") {
        addForeignKeyConstraint(baseColumnNames: "smp_platform_fk", baseTableName: "stats_missing_period", constraintName: "FKpg4wak8mdmfn8vojkmt7pc3uk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform")
    }

    changeSet(author: "galffy (generated)", id: "1645517965908-8") {
        dropForeignKeyConstraint(baseTableName: "stats_missing_period", constraintName: "FKr9lpji4om4xcpy0rl7lwpqcve")
    }

    changeSet(author: "galffy (generated)", id: "1645517965908-9") {
        dropColumn(columnName: "smp_cursor_fk", tableName: "stats_missing_period")
    }

}
