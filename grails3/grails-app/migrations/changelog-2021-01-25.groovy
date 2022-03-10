databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1611561146351-1") {
        dropColumn(columnName: "date_actioned", tableName: "user_org")
    }

    changeSet(author: "klober (generated)", id: "1611561146351-2") {
        dropColumn(columnName: "date_requested", tableName: "user_org")
    }

    changeSet(author: "klober (generated)", id: "1611561146351-3") {
        dropColumn(columnName: "status", tableName: "user_org")
    }

    changeSet(author: "klober (modified)", id: "1611561146351-4") {
        grailsChange {
            change {
                sql.execute("update system_profiler set sp_archive = '1.5' where sp_archive = '1.5-RC'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1611561146351-5") {
        grailsChange {
            change {
                sql.execute("delete from system_profiler where sp_archive in ('1.2', '1.3', '1.4')")
            }
            rollback {}
        }
    }
}