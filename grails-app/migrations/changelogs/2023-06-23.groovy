package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1687518388145-1") {
        createTable(tableName: "subscription_discount_scale") {
            column(autoIncrement: "true", name: "sds_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "subscription_discount_scalePK")
            }

            column(name: "sds_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sp_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "sds_sub_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sp_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "sds_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "sds_discount", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "sds_note", type: "TEXT")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1687518388145-2") {
        createIndex(indexName: "sds_sub_idx", tableName: "subscription_discount_scale") {
            column(name: "sds_sub_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1687518388145-3") {
        addForeignKeyConstraint(baseColumnNames: "sds_sub_fk", baseTableName: "subscription_discount_scale", constraintName: "FKgqm162uhg4p7wnm5xsub2ts1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1687518388145-4") {
        addColumn(tableName: "subscription") {
            column(name: "sub_discount_scale_fk", type: "int8")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1687518388145-5") {
        addForeignKeyConstraint(baseColumnNames: "sub_discount_scale_fk", baseTableName: "subscription", constraintName: "FK79r2xpjceu7njk4wj2g25phek", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sds_id", referencedTableName: "subscription_discount_scale", validate: "true")
    }



}
