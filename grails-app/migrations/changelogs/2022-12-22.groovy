package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1671701835854-1") {
        createIndex(indexName: "tipp_host_platform_url_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_host_platform_url")
        }
    }
}
