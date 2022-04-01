databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1648804786911-1") {
        dropTable(tableName: "system_ticket")
    }

    changeSet(author: "klober (modified)", id: "1648804786911-2") {
        grailsChange {
            change {
                sql.execute("delete from system_setting where set_name = 'StatusUpdateInterval'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1648804786911-3") {
        grailsChange {
            change {
                sql.execute("delete from system_activity_profiler where date_part('year', sap_date_created) in ('2019', '2020')")
                sql.execute("delete from system_event where date_part('year', se_created) in ('2019', '2020')")
            }
            rollback {}
        }
    }

//    changeSet(author: "klober (modified)", id: "1648804786911-4") {
//        grailsChange {
//            change {
//
//            }
//            rollback {}
//        }
//    }
}
