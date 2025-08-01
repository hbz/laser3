package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (modified)", id: "1753947054214-1") {
        dropIndex(indexName: "prov_sortname_idx", tableName: "identifier")
    }

    changeSet(author: "galffy (hand-coded)", id: "1753947054214-2") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE IF EXISTS provider RENAME prov_sortname TO prov_abbreviated_name;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1753947054214-3") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE IF EXISTS vendor RENAME ven_sortname TO ven_abbreviated_name;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1753947054214-4") {
        createIndex(indexName: "prov_abbreviated_name_idx", tableName: "provider") {
            column(name: "prov_abbreviated_name")
        }
    }

    changeSet(author: "galffy (generated)", id: "1753947054214-5") {
        createIndex(indexName: "ven_abbreviated_name_idx", tableName: "vendor") {
            column(name: "ven_abbreviated_name")
        }
    }
}
