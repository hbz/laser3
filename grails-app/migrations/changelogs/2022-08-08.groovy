databaseChangeLog = {

    changeSet(author: "galffy (modified)", id: "1659944322270-1") {
        createTable(tableName: "counter4report") {
            column(autoIncrement: "true", name: "c4r_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "counter4reportPK")
            }

            column(name: "c4r_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c4r_title_fk", type: "BIGINT")

            column(name: "c4r_publisher", type: "TEXT")

            column(name: "c4r_report_from", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "c4r_metric_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c4r_report_institution_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c4r_report_to", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "c4r_report_count", type: "INTEGER") {
                constraints(nullable: "false")
            }

            column(name: "c4r_category", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c4r_report_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c4r_platform_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (modified)", id: "1659944322270-2") {
        createTable(tableName: "counter5report") {
            column(autoIncrement: "true", name: "c5r_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "counter5reportPK")
            }

            column(name: "c5r_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c5r_publisher", type: "TEXT")

            column(name: "c5r_report_from", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "c5r_metric_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c5r_report_institution_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c5r_report_count", type: "INTEGER") {
                constraints(nullable: "false")
            }

            column(name: "c5r_title_fk", type: "BIGINT")

            column(name: "c5r_access_method", type: "VARCHAR(255)")

            column(name: "c5r_access_type", type: "VARCHAR(255)")

            column(name: "c5r_report_to", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "c5r_report_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c5r_platform_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1659944322270-3") {
        createIndex(indexName: "c4r_metric_type_idx", tableName: "counter4report") {
            column(name: "c4r_metric_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1659944322270-4") {
        createIndex(indexName: "c4r_report_from_idx", tableName: "counter4report") {
            column(name: "c4r_report_from")
        }
    }

    changeSet(author: "galffy (generated)", id: "1659944322270-5") {
        createIndex(indexName: "c4r_report_to_idx", tableName: "counter4report") {
            column(name: "c4r_report_to")
        }
    }

    changeSet(author: "galffy (generated)", id: "1659944322270-6") {
        createIndex(indexName: "c4r_report_when_idx", tableName: "counter4report") {
            column(name: "c4r_report_from")

            column(name: "c4r_metric_type")

            column(name: "c4r_report_to")

            column(name: "c4r_report_type")

            column(name: "c4r_title_fk")

            column(name: "c4r_report_institution_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1659944322270-7") {
        createIndex(indexName: "c4r_rt_idx", tableName: "counter4report") {
            column(name: "c4r_report_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1659944322270-8") {
        createIndex(indexName: "c5r_access_method_idx", tableName: "counter5report") {
            column(name: "c5r_access_method")
        }
    }

    changeSet(author: "galffy (generated)", id: "1659944322270-9") {
        createIndex(indexName: "c5r_access_type_idx", tableName: "counter5report") {
            column(name: "c5r_access_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1659944322270-10") {
        createIndex(indexName: "c5r_metric_type_idx", tableName: "counter5report") {
            column(name: "c5r_metric_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1659944322270-11") {
        createIndex(indexName: "c5r_report_from_idx", tableName: "counter5report") {
            column(name: "c5r_report_from")
        }
    }

    changeSet(author: "galffy (generated)", id: "1659944322270-12") {
        createIndex(indexName: "c5r_report_to_idx", tableName: "counter5report") {
            column(name: "c5r_report_to")
        }
    }

    changeSet(author: "galffy (generated)", id: "1659944322270-13") {
        createIndex(indexName: "c5r_report_when_idx", tableName: "counter5report") {
            column(name: "c5r_report_from")

            column(name: "c5r_metric_type")

            column(name: "c5r_report_to")

            column(name: "c5r_report_type")

            column(name: "c5r_title_fk")

            column(name: "c5r_report_institution_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1659944322270-14") {
        createIndex(indexName: "c5r_rt_idx", tableName: "counter5report") {
            column(name: "c5r_report_type")
        }
    }

    changeSet(author: "galffy (modified)", id: "1659944322270-15") {
        addUniqueConstraint(columnNames: "c4r_report_to, c4r_report_from, c4r_metric_type, c4r_report_institution_fk, c4r_platform_fk, c4r_report_type, c4r_title_fk", constraintName: "unique_counter_4_report", tableName: "counter4report")
    }

    changeSet(author: "galffy (modified)", id: "1659944322270-16") {
        addUniqueConstraint(columnNames: "c5r_report_type, c5r_report_to, c5r_report_from, c5r_metric_type, c5r_report_institution_fk, c5r_platform_fk, c5r_title_fk", constraintName: "unique_counter_5_report", tableName: "counter5report")
    }

    changeSet(author: "galffy (hand-coded)", id: "1659944322270-17") {
        createIndex(indexName: "c4r_title_idx", tableName: "counter4report") {
            column(name: "c4r_title_fk")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1659944322270-18") {
        createIndex(indexName: "c4r_plat_idx", tableName: "counter4report") {
            column(name: "c4r_platform_fk")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1659944322270-19") {
        createIndex(indexName: "c4r_ri_idx", tableName: "counter4report") {
            column(name: "c4r_report_institution_fk")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1659944322270-20") {
        createIndex(indexName: "c5r_title_idx", tableName: "counter5report") {
            column(name: "c5r_title_fk")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1659944322270-21") {
        createIndex(indexName: "c5r_plat_idx", tableName: "counter5report") {
            column(name: "c5r_platform_fk")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1659944322270-22") {
        createIndex(indexName: "c5r_ri_idx", tableName: "counter5report") {
            column(name: "c5r_report_institution_fk")
        }
    }

    /*
    changeSet(author: "galffy (hand-coded)", id: "") {
        sqlFile(encoding: "UTF-8", path: "sql/counter4data.sql", relativeToChangelogPath: true, splitStatements: true, stripComments: true)
    }

    changeSet(author: "galffy (hand-coded)", id: "") {
        sqlFile(encoding: "UTF-8", path: "sql/counter5data.sql", relativeToChangelogPath: true, splitStatements: true, stripComments: true)
    }
     */
}
