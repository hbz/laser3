package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1737626994089-1") {
        grailsChange {
            change {
                String query = "delete from reader_number where num_semester_rv_fk in (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdc_description = 'semester' and rdv_value ilike 's%')"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1737626994089-2") {
        grailsChange {
            change {
                String query = "delete from refdata_value where rdv_id in (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdc_description = 'semester' and rdv_value ilike 's%')"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }
}
