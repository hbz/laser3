package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1671701835854-1") {
        createIndex(indexName: "tipp_host_platform_url_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_host_platform_url")
        }
    }

    changeSet(author: "galffy (generated)", id: "1671701835854-2") {
        addColumn(tableName: "subscription") {
            column(name: "sub_reference_year", type: "bytea")
        }
    }

    changeSet(author: "galffy (generated)", id: "1671701835854-3") {
        createIndex(indexName: "sub_reference_year_idx", tableName: "subscription") {
            column(name: "sub_reference_year")
        }
    }
}
