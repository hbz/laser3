databaseChangeLog = {

    changeSet(author: "galffy (modified)", id: "1630585340318-1") {
        addUniqueConstraint(columnNames: "c4r_report_to, c4r_report_from, c4r_metric_type, c4r_report_institution_fk, c4r_platform_fk, c4r_report_type, c4r_title_fk", constraintName: "unique_counter_4_report", tableName: "counter4report")
    }

    changeSet(author: "galffy (modified)", id: "1630585340318-2") {
        addUniqueConstraint(columnNames: "c5r_report_type, c5r_report_to, c5r_report_from, c5r_metric_type, c5r_report_institution_fk, c5r_platform_fk, c5r_title_fk", constraintName: "unique_counter_5_report", tableName: "counter5report")
    }

    changeSet(author: "galffy (generated)", id: "1630585340318-3") {
        addForeignKeyConstraint(baseColumnNames: "lsc_platform_fk", baseTableName: "laser_stats_cursor", constraintName: "FKdewt128r21y5ul8i9gtpv9yfr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform")
    }

    changeSet(author: "galffy (generated)", id: "1630585340318-4") {
        addForeignKeyConstraint(baseColumnNames: "lsc_customer_fk", baseTableName: "laser_stats_cursor", constraintName: "FKp9dtf51va8vxo4j6apb5evchy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org")
    }
}
