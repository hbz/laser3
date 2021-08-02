databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1627897549314-1") {
        grailsChange {
            change {
                sql.execute("update property_definition set pd_multiple_occurrence = true where pd_name = 'Cost unit'")
            }
            rollback {}
        }
    }

}