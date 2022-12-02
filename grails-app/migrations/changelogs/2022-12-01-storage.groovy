databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1669891008399-1") {
        addColumn(tableName: "counter4report") {
            column(name: "c4r_doi", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-2") {
        addColumn(tableName: "counter4report") {
            column(name: "c4r_isbn", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-3") {
        addColumn(tableName: "counter4report") {
            column(name: "c4r_online_identifier", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-4") {
        addColumn(tableName: "counter4report") {
            column(name: "c4r_print_identifier", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-5") {
        addColumn(tableName: "counter4report") {
            column(name: "c4r_proprietary_identifier", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-6") {
        addColumn(tableName: "counter5report") {
            column(name: "c5r_doi", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-7") {
        addColumn(tableName: "counter5report") {
            column(name: "c5r_isbn", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-8") {
        addColumn(tableName: "counter5report") {
            column(name: "c5r_online_identifier", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-9") {
        addColumn(tableName: "counter5report") {
            column(name: "c5r_print_identifier", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-10") {
        addColumn(tableName: "counter5report") {
            column(name: "c5r_proprietary_identifier", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-11") {
        createIndex(indexName: "c4r_doi_idx", tableName: "counter4report") {
            column(name: "c4r_doi")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-12") {
        createIndex(indexName: "c4r_isbn_idx", tableName: "counter4report") {
            column(name: "c4r_isbn")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-13") {
        createIndex(indexName: "c4r_online_identifier_idx", tableName: "counter4report") {
            column(name: "c4r_online_identifier")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-14") {
        createIndex(indexName: "c4r_print_identifier_idx", tableName: "counter4report") {
            column(name: "c4r_print_identifier")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-15") {
        createIndex(indexName: "c4r_prop_ident_idx", tableName: "counter4report") {
            column(name: "c4r_proprietary_identifier")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-16") {
        createIndex(indexName: "c5r_doi_idx", tableName: "counter5report") {
            column(name: "c5r_doi")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-17") {
        createIndex(indexName: "c5r_isbn_idx", tableName: "counter5report") {
            column(name: "c5r_isbn")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-18") {
        createIndex(indexName: "c5r_online_identifier_idx", tableName: "counter5report") {
            column(name: "c5r_online_identifier")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-19") {
        createIndex(indexName: "c5r_print_identifier_idx", tableName: "counter5report") {
            column(name: "c5r_print_identifier")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-20") {
        createIndex(indexName: "c5r_prop_ident_idx", tableName: "counter5report") {
            column(name: "c5r_proprietary_identifier")
        }
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-21") {
        dropUniqueConstraint(constraintName: "unique_counter_4_report", tableName: "counter4report")
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-22") {
        dropUniqueConstraint(constraintName: "unique_counter_5_report", tableName: "counter5report")
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-23") {
        dropColumn(columnName: "c4r_title_guid", tableName: "counter4report")
    }

    changeSet(author: "galffy (generated)", id: "1669891008399-24") {
        dropColumn(columnName: "c5r_title_guid", tableName: "counter5report")
    }

    changeSet(author: "galffy (hand-coded)", id: "1669891008399-25") {
        grailsChange {
            change {
                sql.execute("create unique index unique_counter_4_report_jr5 on counter4report (c4r_report_from, c4r_report_to, c4r_report_type, c4r_metric_type, c4r_platform_guid, c4r_report_institution_guid, coalesce(c4r_online_identifier, null), coalesce(c4r_print_identifier, null), coalesce(c4r_isbn, null), coalesce(c4r_doi, null), c4r_yop) where c4r_yop is not null")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1669891008399-26") {
        grailsChange {
            change {
                sql.execute("create unique index unique_counter_4_report_other on counter4report (c4r_report_from, c4r_report_to, c4r_report_type, c4r_metric_type, c4r_platform_guid, c4r_report_institution_guid, coalesce(c4r_online_identifier, null), coalesce(c4r_print_identifier, null), coalesce(c4r_isbn, null), coalesce(c4r_doi, null)) where c4r_yop is null")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1669891008399-27") {
        grailsChange {
            change {
                sql.execute('create unique index unique_counter_5_report_tr_j4 on counter5report (c5r_report_from, c5r_report_to, c5r_report_type, c5r_metric_type, c5r_platform_guid, c5r_report_institution_guid, coalesce(c5r_online_identifier, null), coalesce(c5r_print_identifier, null), coalesce(c5r_isbn, null), coalesce(c5r_doi, null), c5r_yop) where c5r_yop is not null')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1669891008399-28") {
        grailsChange {
            change {
                sql.execute('create unique index unique_counter_5_report_other on counter5report (c5r_report_from, c5r_report_to, c5r_report_type, c5r_metric_type, c5r_platform_guid, c5r_report_institution_guid, coalesce(c5r_online_identifier, null), coalesce(c5r_print_identifier, null), coalesce(c5r_isbn, null), coalesce(c5r_doi, null)) where c5r_yop is null')
            }
            rollback {}
        }
    }
}
