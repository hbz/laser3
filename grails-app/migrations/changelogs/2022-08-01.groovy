package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1659345821169-1") {
        grailsChange {
            change {
                sql.execute("TRUNCATE TABLE audit_log RESTART IDENTITY")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (generated)", id: "1659345821169-2") {
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
}
