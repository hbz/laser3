package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1717761752759-1") {
        grailsChange {
            change {
                String query = "update refdata_value set rdv_value = 'Invoicing Contact' where rdv_value = 'Functional Contact Billing Adress'"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1717761752759-2") {
        /*
        grailsChange {
            change {
                String query = "delete from refdata_value where rdv_value = 'Functional Contact Billing Adress'"
                sql.execute(query)
            }
            rollback {}
        }
        */
    }
}
