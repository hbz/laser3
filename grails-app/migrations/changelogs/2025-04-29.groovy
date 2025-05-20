package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1745917470859-1") {
        dropColumn(columnName: "org_comment", tableName: "org")
    }

    changeSet(author: "klober (generated)", id: "1745917470859-2") {
        dropColumn(columnName: "org_ip_range", tableName: "org")
    }

    changeSet(author: "klober (generated)", id: "1745917470859-3") {
        dropColumn(columnName: "org_scope", tableName: "org")
    }

    changeSet(author: "klober (modified)", id: "1745917470859-4") {
        grailsChange {
            change {
                sql.execute("delete from system_profiler where sp_archive = '3.3'")
            }
            rollback {}
        }
    }
}
