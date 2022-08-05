package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1659345821169-1") {
        grailsChange {
            change {
                sql.execute("TRUNCATE TABLE audit_log RESTART IDENTITY")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1659345821169-2") {
        grailsChange {
            change {
                 de.laser.remote.FTControl.list().each{
                    try {
                        Class.forName(it.domainClassName)
                    } catch (Exception e) {
                        sql.execute("delete from ftcontrol where ftc_id = " + it.id )
                    }
                }
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1659345821169-3") {
        grailsChange {
            change {
                sql.execute("delete from system_event where se_token = 'DBDD_SERVICE_START_COLLECT_DASHBOARD_DATA'")
                sql.execute("delete from system_event where se_token = 'DBDD_SERVICE_END_COLLECT_DASHBOARD_DATA'")
                sql.execute("delete from system_event where se_token = 'DBDD_SERVICE_START_TRANSACTION'")
                sql.execute("delete from system_event where se_token = 'DBDD_SERVICE_END_TRANSACTION'")

            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1659345821169-4") {
        grailsChange {
            change {
                sql.execute("delete from system_event where se_token = 'DBDD_SERVICE_PROCESSING_2'")
                sql.execute("delete from system_event where se_token = 'DBDD_SERVICE_COMPLETE_2'")
                sql.execute("delete from system_event where se_token = 'DBDD_SERVICE_COMPLETE_3'")
            }
            rollback {}
        }
    }
}
