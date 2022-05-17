package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1646043223247-1") {
        grailsChange {
            change {
                sql.execute("update refdata_value set rdv_value = 'Technical Support' where rdv_value = 'Technichal Support'")
            }
            rollback {}
        }
    }

}
