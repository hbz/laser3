databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1626942455650-1") {
        createTable(tableName: "counter4api_source") {
            column(autoIncrement: "true", name: "c4as_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "counter4api_sourcePK")
            }

            column(name: "c4as_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c4as_platform_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c4as_provider_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c4as_base_url", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c4as_arguments", type: "TEXT")
        }
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-2") {
        createTable(tableName: "counter4report") {
            column(autoIncrement: "true", name: "c4r_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "counter4reportPK")
            }

            column(name: "c4r_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c4r_title_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c4r_publisher", type: "TEXT") {
                constraints(nullable: "false")
            }

            column(name: "c4r_month", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c4r_platform_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c4r_report_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c4r_year", type: "BYTEA") {
                constraints(nullable: "false")
            }

            column(name: "c4r_report_institution_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-3") {
        createTable(tableName: "counter5api_source") {
            column(autoIncrement: "true", name: "c5as_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "counter5api_sourcePK")
            }

            column(name: "c5as_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c5as_platform_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c5as_provider_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c5as_base_url", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c5as_arguments", type: "TEXT")
        }
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-4") {
        createTable(tableName: "counter5report") {
            column(autoIncrement: "true", name: "c5r_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "counter5reportPK")
            }

            column(name: "c5r_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c5r_title_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c5r_publisher", type: "TEXT") {
                constraints(nullable: "false")
            }

            column(name: "c5r_access_method", type: "VARCHAR(255)")

            column(name: "c5r_metric_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c5r_access_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c5r_month", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c5r_platform_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c5r_report_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c5r_year", type: "BYTEA") {
                constraints(nullable: "false")
            }

            column(name: "c5r_report_institution_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-5") {
        createIndex(indexName: "c4r_ri_idx", tableName: "counter4report") {
            column(name: "c4r_report_institution_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-6") {
        createIndex(indexName: "c4r_rt_idx", tableName: "counter4report") {
            column(name: "c4r_report_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-7") {
        createIndex(indexName: "c4r_title_idx", tableName: "counter4report") {
            column(name: "c4r_title_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-8") {
        createIndex(indexName: "c5r_ri_idx", tableName: "counter5report") {
            column(name: "c5r_report_institution_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-9") {
        createIndex(indexName: "c5r_rt_idx", tableName: "counter5report") {
            column(name: "c5r_report_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-10") {
        createIndex(indexName: "c5r_title_idx", tableName: "counter5report") {
            column(name: "c5r_title_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-11") {
        addForeignKeyConstraint(baseColumnNames: "c5r_platform_fk", baseTableName: "counter5report", constraintName: "FK31vd6v6su5u69nt51asgag1nn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform")
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-12") {
        addForeignKeyConstraint(baseColumnNames: "c5as_provider_fk", baseTableName: "counter5api_source", constraintName: "FK44p0juwe00rya0aq4ifk7x8hs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org")
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-13") {
        addForeignKeyConstraint(baseColumnNames: "c4r_platform_fk", baseTableName: "counter4report", constraintName: "FKddd9wc7r99k20m27s5i6gvssk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform")
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-14") {
        addForeignKeyConstraint(baseColumnNames: "c4as_provider_fk", baseTableName: "counter4api_source", constraintName: "FKfbr4x2ya24u4nred3edjbtnh0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org")
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-15") {
        addForeignKeyConstraint(baseColumnNames: "c5r_title_fk", baseTableName: "counter5report", constraintName: "FKk2nvdfq2kk5rvvt05p1calp1t", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-16") {
        addForeignKeyConstraint(baseColumnNames: "the_tipp_fk", baseTableName: "title_history_event", constraintName: "FK9muxgnb84xa1vct4hx3m543bo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-17") {
        addForeignKeyConstraint(baseColumnNames: "c5r_report_institution_fk", baseTableName: "counter5report", constraintName: "FKkli0y9981y5c8nd8iyklnp5lh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org")
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-18") {
        addForeignKeyConstraint(baseColumnNames: "c5as_platform_fk", baseTableName: "counter5api_source", constraintName: "FKlgwnay77sw38y6d76fvyhnney", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform")
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-19") {
        addForeignKeyConstraint(baseColumnNames: "c4r_report_institution_fk", baseTableName: "counter4report", constraintName: "FKoayxjnyphov60bfduk9xmcmfu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org")
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-20") {
        addForeignKeyConstraint(baseColumnNames: "c4r_title_fk", baseTableName: "counter4report", constraintName: "FKpai9tby9ugelgstviowr2v70q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1626942455650-21") {
        addForeignKeyConstraint(baseColumnNames: "c4as_platform_fk", baseTableName: "counter4api_source", constraintName: "FKrgdyiouc805gw7ui6up7svj73", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform")
    }
}
