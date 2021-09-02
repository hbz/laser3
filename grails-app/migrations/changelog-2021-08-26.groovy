databaseChangeLog = {

    changeSet(author: "galffy (modified)", id: "1629958945001-1") {
        addUniqueConstraint(columnNames: "lsc_customer_fk, lsc_platform_fk, lsc_report_id", constraintName: "lsc_unique_report_per_customer", tableName: "laser_stats_cursor")
    }

}
