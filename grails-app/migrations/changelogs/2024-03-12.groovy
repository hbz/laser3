package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1710230772753-1") {
        grailsChange {
            change {
                sql.execute("delete from system_event where date_part('Year', se_created) = 2023")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1710230772753-2") {
        grailsChange {
            change {
                sql.execute("delete from system_activity_profiler where date_part('Year', sap_date_created) = 2023")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1710230772753-3") {
        grailsChange {
            change {
                sql.execute("delete from system_profiler where sp_archive = '3.2'")
            }
            rollback {}
        }
    }
}
