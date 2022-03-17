databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1605526676202-1") {
        grailsChange {
            change {
                sql.execute("UPDATE audit_log SET actor = 'SYS' where actor = 'system'")
            }
            rollback {}
        }
    }
    changeSet(author: "klober (modified)", id: "1605526676202-2") {
        grailsChange {
            change {
                sql.execute("UPDATE audit_log SET actor = 'anonymised' where actor is not null and actor not in ('anonymised', 'SYS', 'N/A')")
            }
            rollback {}
        }
    }
}
