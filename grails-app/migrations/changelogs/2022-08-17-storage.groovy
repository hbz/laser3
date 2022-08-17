databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1660716827200-1") {
        createTable(tableName: "counter4report") {
            column(autoIncrement: "true", name: "c4r_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "counter4reportPK")
            }

            column(name: "c4r_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "c4r_publisher", type: "TEXT")

            column(name: "c4r_report_from", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "c4r_metric_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c4r_platform_guid", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c4r_report_count", type: "INTEGER") {
                constraints(nullable: "false")
            }

            column(name: "c4r_report_institution_guid", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c4r_report_to", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "c4r_category", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c4r_title_guid", type: "VARCHAR(255)")

            column(name: "c4r_report_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-2") {
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

            column(name: "c5r_platform_guid", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c5r_report_count", type: "INTEGER") {
                constraints(nullable: "false")
            }

            column(name: "c5r_report_institution_guid", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "c5r_access_method", type: "VARCHAR(255)")

            column(name: "c5r_access_type", type: "VARCHAR(255)")

            column(name: "c5r_report_to", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "c5r_title_guid", type: "VARCHAR(255)")

            column(name: "c5r_report_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-3") {
        createIndex(indexName: "c4r_metric_type_idx", tableName: "counter4report") {
            column(name: "c4r_metric_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-4") {
        createIndex(indexName: "c4r_plat_idx", tableName: "counter4report") {
            column(name: "c4r_platform_guid")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-5") {
        createIndex(indexName: "c4r_report_from_idx", tableName: "counter4report") {
            column(name: "c4r_report_from")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-6") {
        createIndex(indexName: "c4r_report_to_idx", tableName: "counter4report") {
            column(name: "c4r_report_to")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-7") {
        createIndex(indexName: "c4r_report_when_idx", tableName: "counter4report") {
            column(name: "c4r_report_from")

            column(name: "c4r_metric_type")

            column(name: "c4r_report_institution_guid")

            column(name: "c4r_report_to")

            column(name: "c4r_title_guid")

            column(name: "c4r_report_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-8") {
        createIndex(indexName: "c4r_ri_idx", tableName: "counter4report") {
            column(name: "c4r_report_institution_guid")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-9") {
        createIndex(indexName: "c4r_rt_idx", tableName: "counter4report") {
            column(name: "c4r_report_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-10") {
        createIndex(indexName: "c4r_title_idx", tableName: "counter4report") {
            column(name: "c4r_title_guid")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-11") {
        createIndex(indexName: "c5r_access_method_idx", tableName: "counter5report") {
            column(name: "c5r_access_method")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-12") {
        createIndex(indexName: "c5r_access_type_idx", tableName: "counter5report") {
            column(name: "c5r_access_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-13") {
        createIndex(indexName: "c5r_metric_type_idx", tableName: "counter5report") {
            column(name: "c5r_metric_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-14") {
        createIndex(indexName: "c5r_plat_idx", tableName: "counter5report") {
            column(name: "c5r_platform_guid")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-15") {
        createIndex(indexName: "c5r_report_from_idx", tableName: "counter5report") {
            column(name: "c5r_report_from")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-16") {
        createIndex(indexName: "c5r_report_to_idx", tableName: "counter5report") {
            column(name: "c5r_report_to")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-17") {
        createIndex(indexName: "c5r_report_when_idx", tableName: "counter5report") {
            column(name: "c5r_report_from")

            column(name: "c5r_metric_type")

            column(name: "c5r_report_institution_guid")

            column(name: "c5r_report_to")

            column(name: "c5r_title_guid")

            column(name: "c5r_report_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-18") {
        createIndex(indexName: "c5r_ri_idx", tableName: "counter5report") {
            column(name: "c5r_report_institution_guid")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-19") {
        createIndex(indexName: "c5r_rt_idx", tableName: "counter5report") {
            column(name: "c5r_report_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1660716827200-20") {
        createIndex(indexName: "c5r_title_idx", tableName: "counter5report") {
            column(name: "c5r_title_guid")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1660716827200-21") {
        addUniqueConstraint(columnNames: "c4r_report_to, c4r_report_from, c4r_metric_type, c4r_report_institution_guid, c4r_platform_guid, c4r_report_type, c4r_title_guid", constraintName: "unique_counter_4_report", tableName: "counter4report")
    }

    changeSet(author: "galffy (hand-coded)", id: "1660716827200-22") {
        addUniqueConstraint(columnNames: "c5r_report_type, c5r_report_to, c5r_report_from, c5r_metric_type, c5r_report_institution_guid, c5r_platform_guid, c5r_title_guid", constraintName: "unique_counter_5_report", tableName: "counter5report")
    }

}
