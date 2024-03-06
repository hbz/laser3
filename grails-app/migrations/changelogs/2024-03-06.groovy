package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1709727653609-1") {
        grailsChange {
            change {
                int updated = sql.executeUpdate("update doc_context set dc_share_conf_fk = (select rdv_id from refdata_value where rdv_value = 'only for author organisation') where dc_share_conf_fk = (select rdv_id from refdata_value where rdv_value = 'only for consortia members');")
                confirm("update doc_context set dc_share_conf_fk = (select rdv_id from refdata_value where rdv_value = 'only for author organisation') where dc_share_conf_fk = (select rdv_id from refdata_value where rdv_value = 'only for consortia members'): ${updated}")
                changeSet.setComments("update doc_context set dc_share_conf_fk = (select rdv_id from refdata_value where rdv_value = 'only for author organisation') where dc_share_conf_fk = (select rdv_id from refdata_value where rdv_value = 'only for consortia members'): ${updated}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1709727653609-2") {
        grailsChange {
            change {
                int updated = sql.executeUpdate("delete from refdata_value where rdv_value = 'only for consortia members';")
                confirm("delete from refdata_value where rdv_value = 'only for consortia members': ${updated}")
                changeSet.setComments("delete from refdata_value where rdv_value = 'only for consortia members': ${updated}")
            }
            rollback {}
        }
    }

}
