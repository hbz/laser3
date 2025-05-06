package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1746534094963-1") {
        grailsChange {
            change {
                String headAccessServices = "select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Head Access Services' and rdc_description = 'person.position'"
                String erwerbungsleitung = "select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Erwerbungsleitung' and rdc_description = 'person.position'"
                String query = "update person_role set pr_position_type_rv_fk = (${headAccessServices}) where pr_position_type_rv_fk = (${erwerbungsleitung})"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count} updated"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1746534094963-2") {
        grailsChange {
            change {
                String erwerbungsleitung = "select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Erwerbungsleitung' and rdc_description = 'person.position'"
                String query = "delete from refdata_value where rdv_id = (${erwerbungsleitung})"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count} deleted"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

}
