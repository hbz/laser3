import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.system.SystemEvent

databaseChangeLog = {

    changeSet(author: "djebeniani (modified)", id: "1611307923419-1") {
        grailsChange {
            change {
                sql.execute("DELETE FROM issue_entitlement_group_item where igi_ie_fk in (select ie_id FROM issue_entitlement JOIN refdata_value rv ON issue_entitlement.ie_status_rv_fk = rv.rdv_id WHERE rdv_value = 'Deleted')")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1611307923419-2") {
        grailsChange {
            change {
                List matches = []
                User.findAll().each{ usr ->
                    usr.affiliations.groupBy {it.org }.each { affils ->
                        if (affils.value.size() > 1) {
                            UserOrg winner
                            affils.value.each {uo ->
                                if (uo.formalRole.authority == 'INST_ADM') {
                                    winner = uo
                                }
                                else if (uo.formalRole.authority == 'INST_EDITOR' && !winner) {
                                    winner = uo
                                }
                            }
                            if (winner) {
                                affils.value.remove(winner)
                                matches.addAll(affils.value)
                            }
                        }
                    }
                }

                List idList = matches.collect { it.id }
                if (idList) {
                    UserOrg.executeUpdate('delete from UserOrg where id in :idList', [idList: idList])
                    SystemEvent.createEvent('DBM_SCRIPT_INFO', ['changeset': '1611307923419-2', 'count': idList.size(), 'ids': idList])
                }
            }
        }
    }
}
