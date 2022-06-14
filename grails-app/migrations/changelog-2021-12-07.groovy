databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1638884367017-1") {
        createIndex(indexName: "c4r_plat_idx", tableName: "counter4report") {
            column(name: "c4r_platform_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1638884367017-2") {
        createIndex(indexName: "c5r_plat_idx", tableName: "counter5report") {
            column(name: "c5r_platform_fk")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1638884367017-3") {
        dropIndex(indexName: "pending_change_oid_idx", tableName: "pending_change")

        grailsChange {
            change {
                sql.execute("CREATE INDEX pending_change_oid_idx ON pending_change USING GIN (regexp_split_to_array(pc_oid, ':'));")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1638884367017-4") {
        createIndex(indexName: "pending_change_ts_idx", tableName: "pending_change") {
            column(name: "pc_ts")
        }
    }
}
