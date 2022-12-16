package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1671116531659-1") {
        addColumn(tableName: "counter4report") {
            column(name: "c4r_identifier_hash", type: "text")
        }
    }

    changeSet(author: "galffy (generated)", id: "1671116531659-2") {
        addColumn(tableName: "counter5report") {
            column(name: "c5r_identifier_hash", type: "text")
        }
    }

    changeSet(author: "galffy (generated)", id: "1671116531659-3") {
        createIndex(indexName: "c4r_idhash_idx", tableName: "counter4report") {
            column(name: "c4r_identifier_hash")
        }
    }

    changeSet(author: "galffy (generated)", id: "1671116531659-4") {
        createIndex(indexName: "c5r_idhash_idx", tableName: "counter5report") {
            column(name: "c5r_identifier_hash")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1671116531659-5") {
        dropIndex(indexName: "unique_counter_4_report_jr5", tableName: "counter4report")
    }

    changeSet(author: "galffy (hand-coded)", id: "1671116531659-6") {
        dropIndex(indexName: "unique_counter_4_report_other", tableName: "counter4report")
    }

    changeSet(author: "galffy (hand-coded)", id: "1671116531659-7") {
        dropIndex(indexName: "unique_counter_5_report_tr_j4", tableName: "counter5report")
    }

    changeSet(author: "galffy (hand-coded)", id: "1671116531659-8") {
        dropIndex(indexName: "unique_counter_5_report_other", tableName: "counter5report")
    }

    changeSet(author: "galffy (hand-coded)", id: "1671116531659-9") {
        grailsChange {
            change {
                sql.execute("create unique index unique_counter_4_report on counter4report (c4r_report_from, c4r_report_to, c4r_report_type, c4r_metric_type, c4r_platform_guid, c4r_report_institution_guid, c4r_identifier_hash)")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1671116531659-10") {
        grailsChange {
            change {
                sql.execute('create unique index unique_counter_5_report on counter5report (c5r_report_from, c5r_report_to, c5r_report_type, c5r_metric_type, c5r_platform_guid, c5r_report_institution_guid, c5r_identifier_hash)')
            }
            rollback {}
        }
    }
}
