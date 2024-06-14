package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1709727653609-1") {
        grailsChange {
            change {
                String query = "update doc_context set dc_share_conf_fk = (select rdv_id from refdata_value where rdv_value = 'only for author organisation') where dc_share_conf_fk = (select rdv_id from refdata_value where rdv_value = 'only for consortia members');"
                int updated = sql.executeUpdate(query)
                confirm("${query}: ${updated}")
                changeSet.setComments("${query}: ${updated}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1709727653609-2") {
        grailsChange {
            change {
                String query = "delete from refdata_value where rdv_value = 'only for consortia members';"
                int updated = sql.executeUpdate(query)
                confirm("${query}: ${updated}")
                changeSet.setComments("${query}: ${updated}")
            }
            rollback {}
        }
    }

}
