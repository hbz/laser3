databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1668154015874-1") {
        dropUniqueConstraint(constraintName: "unique_counter_4_report", tableName: "counter4report")
    }

    changeSet(author: "galffy (generated)", id: "1668154015874-2") {
        dropUniqueConstraint(constraintName: "unique_counter_5_report", tableName: "counter5report")
    }

    changeSet(author: "galffy (hand-coded)", id: "1668154015874-3") {
        grailsChange {
            change {
                sql.execute('create unique index unique_counter_4_report_jr5 on counter4report (c4r_report_from, c4r_report_to, c4r_report_type, c4r_metric_type, c4r_platform_guid, c4r_report_institution_guid, c4r_title_guid, c4r_yop) where c4r_yop is not null')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1668154015874-4") {
        grailsChange {
            change {
                sql.execute('create unique index unique_counter_4_report_other on counter4report (c4r_report_from, c4r_report_to, c4r_report_type, c4r_metric_type, c4r_platform_guid, c4r_report_institution_guid, c4r_title_guid) where c4r_yop is null')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1668154015874-5") {
        grailsChange {
            change {
                sql.execute('create unique index unique_counter_5_report_tr_j4 on counter5report (c5r_report_from, c5r_report_to, c5r_report_type, c5r_metric_type, c5r_platform_guid, c5r_report_institution_guid, c5r_title_guid, c5r_yop) where c5r_yop is not null')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1668154015874-6") {
        grailsChange {
            change {
                sql.execute('create unique index unique_counter_5_report_other on counter5report (c5r_report_from, c5r_report_to, c5r_report_type, c5r_metric_type, c5r_platform_guid, c5r_report_institution_guid, c5r_title_guid) where c5r_yop is null')
            }
            rollback {}
        }
    }
}
