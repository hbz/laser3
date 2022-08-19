package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1659013557968-3") {
        dropForeignKeyConstraint(baseTableName: "counter5report", constraintName: "FK31vd6v6su5u69nt51asgag1nn")
    }

    changeSet(author: "galffy (generated)", id: "1659013557968-4") {
        dropForeignKeyConstraint(baseTableName: "counter4report", constraintName: "FKddd9wc7r99k20m27s5i6gvssk")
    }

    changeSet(author: "galffy (generated)", id: "1659013557968-5") {
        dropForeignKeyConstraint(baseTableName: "counter5report", constraintName: "FKk2nvdfq2kk5rvvt05p1calp1t")
    }

    changeSet(author: "galffy (generated)", id: "1659013557968-6") {
        dropForeignKeyConstraint(baseTableName: "counter5report", constraintName: "FKkli0y9981y5c8nd8iyklnp5lh")
    }

    changeSet(author: "galffy (generated)", id: "1659013557968-7") {
        dropForeignKeyConstraint(baseTableName: "counter4report", constraintName: "FKoayxjnyphov60bfduk9xmcmfu")
    }

    changeSet(author: "galffy (generated)", id: "1659013557968-8") {
        dropForeignKeyConstraint(baseTableName: "counter4report", constraintName: "FKpai9tby9ugelgstviowr2v70q")
    }

    changeSet(author: "galffy (generated)", id: "1659013557968-9") {
        dropUniqueConstraint(constraintName: "unique_counter_4_report", tableName: "counter4report")
    }

    changeSet(author: "galffy (generated)", id: "1659013557968-10") {
        dropUniqueConstraint(constraintName: "unique_counter_5_report", tableName: "counter5report")
    }

    changeSet(author: "galffy (generated)", id: "1659013557968-11") {
        dropTable(tableName: "counter4report")
    }

    changeSet(author: "galffy (generated)", id: "1659013557968-12") {
        dropTable(tableName: "counter5report")
    }

    changeSet(author: "galffy (hand-coded)", id: "1659013557968-13") {
        grailsChange {
            change {
                sql.execute("truncate table laser_stats_cursor;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1659013557968-14") {
        grailsChange {
            change {
                sql.execute("truncate table stats_missing_period;")
            }
            rollback {}
        }
    }
}
