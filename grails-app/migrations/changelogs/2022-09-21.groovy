package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1663742981531-1") {
        createIndex(indexName: "ddc_pkg_idx", tableName: "dewey_decimal_classification") {
            column(name: "ddc_pkg_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1663742981531-2") {
        createIndex(indexName: "ddc_rv_idx", tableName: "dewey_decimal_classification") {
            column(name: "ddc_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1663742981531-3") {
        createIndex(indexName: "ddc_tipp_idx", tableName: "dewey_decimal_classification") {
            column(name: "ddc_tipp_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1663742981531-4") {
        createIndex(indexName: "ddc_tipp_rv_idx", tableName: "dewey_decimal_classification") {
            column(name: "ddc_tipp_fk")

            column(name: "ddc_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1663742981531-5") {
        createIndex(indexName: "lang_pkg_idx", tableName: "language") {
            column(name: "lang_pkg_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1663742981531-6") {
        createIndex(indexName: "lang_rv_idx", tableName: "language") {
            column(name: "lang_rv_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1663742981531-7") {
        createIndex(indexName: "lang_tipp_idx", tableName: "language") {
            column(name: "lang_tipp_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1663742981531-8") {
        createIndex(indexName: "lang_tipp_rv_idx", tableName: "language") {
            column(name: "lang_tipp_fk")

            column(name: "lang_rv_fk")
        }
    }
}
