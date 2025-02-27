package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1740132278348-1") {
        createTable(tableName: "counter_check") {
            column(autoIncrement: "true", name: "cc_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "counter_checkPK")
            }

            column(name: "cc_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "cc_org_fk", type: "BIGINT")

            column(name: "cc_err_mess", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "cc_call_error", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "cc_url", type: "TEXT") {
                constraints(nullable: "false")
            }

            column(name: "cc_timestamp", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "cc_platform_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "cc_requestor_id", type: "VARCHAR(255)")

            column(name: "cc_customer_id", type: "VARCHAR(255)")

            column(name: "cc_err_token", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1740132278348-2") {
        createIndex(indexName: "cc_customer_idx", tableName: "counter_check") {
            column(name: "cc_customer_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1740132278348-3") {
        createIndex(indexName: "cc_customer_requestor_platform_idx", tableName: "counter_check") {
            column(name: "cc_platform_fk")

            column(name: "cc_requestor_id")

            column(name: "cc_customer_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1740132278348-4") {
        createIndex(indexName: "cc_org_idx", tableName: "counter_check") {
            column(name: "cc_org_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1740132278348-5") {
        createIndex(indexName: "cc_platform_idx", tableName: "counter_check") {
            column(name: "cc_platform_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1740132278348-6") {
        createIndex(indexName: "cc_requestor_idx", tableName: "counter_check") {
            column(name: "cc_requestor_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1740132278348-7") {
        addForeignKeyConstraint(baseColumnNames: "cc_org_fk", baseTableName: "counter_check", constraintName: "FK120yb8q3d128kfrogpefxo4sh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1740132278348-8") {
        addForeignKeyConstraint(baseColumnNames: "cc_platform_fk", baseTableName: "counter_check", constraintName: "FKocl6oloxmuujd2etf38f9r0bv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1740132278348-9") {
        dropForeignKeyConstraint(baseTableName: "sushi_call_error", constraintName: "FK9r4plw01rei1l3ggrme11ty4i")
    }

    changeSet(author: "galffy (generated)", id: "1740132278348-10") {
        dropForeignKeyConstraint(baseTableName: "sushi_call_error", constraintName: "FKqssjx02pqddk1i30h503kfbbi")
    }

    changeSet(author: "galffy (generated)", id: "1740132278348-11") {
        dropTable(tableName: "sushi_call_error")
    }

    changeSet(author: "klober (modified)", id: "1740132278348-12") {
        grailsChange {
            change {
                String query = "delete from org_setting where os_key_enum = 'GASCO_ENTRY';"
                sql.executeUpdate(query)
                String c = query + ' -> ' + sql.getUpdateCount()
                confirm(c)
                changeSet.setComments(c)
            }
        }
    }
}
