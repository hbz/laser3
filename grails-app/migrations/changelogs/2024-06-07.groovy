package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1717761752759-1") {
        grailsChange {
            change {
                String query = "update person_role set pr_function_type_rv_fk = (select rdv_id from refdata_value where rdv_value = 'Invoicing Contact') where pr_function_type_rv_fk = (select rdv_id from refdata_value where rdv_value = 'Functional Contact Billing Adress')"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1717761752759-2") {
        grailsChange {
            change {
                String query = "delete from refdata_value where rdv_value = 'Functional Contact Billing Adress'"
                sql.execute(query)
            }
            rollback {}
        }
    }
}
