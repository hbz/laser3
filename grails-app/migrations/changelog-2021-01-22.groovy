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

    changeSet(author: "agalffy (generated)", id: "1611307923419-3") {
        addColumn(tableName: "pending_change") {
            column(name: "pc_tc_fk", type: "int8")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611307923419-4") {
        addColumn(tableName: "pending_change") {
            column(name: "pc_tipp_fk", type: "int8")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611307923419-5") {
        createIndex(indexName: "pending_change_tc_idx", tableName: "pending_change") {
            column(name: "pc_tc_fk")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611307923419-6") {
        createIndex(indexName: "pending_change_tipp_idx", tableName: "pending_change") {
            column(name: "pc_tipp_fk")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611307923419-7") {
        addForeignKeyConstraint(baseColumnNames: "pc_tipp_fk", baseTableName: "pending_change", constraintName: "FKm0vthqihnnrirmglv4samn8a7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform")
    }

    changeSet(author: "agalffy (generated)", id: "1611307923419-8") {
        addForeignKeyConstraint(baseColumnNames: "pc_tc_fk", baseTableName: "pending_change", constraintName: "FKpfvhr4eht8rf8mjpfvsoup0ky", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tc_id", referencedTableName: "tippcoverage")
    }

    changeSet(author: "agalffy (generated)", id: "1611307923419-9") {
        addColumn(tableName: "pending_change") {
            column(name: "pc_pi_fk", type: "int8")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611307923419-10") {
        createIndex(indexName: "pending_change_pi_idx", tableName: "pending_change") {
            column(name: "pc_pi_fk")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611307923419-11") {
        addForeignKeyConstraint(baseColumnNames: "pc_pi_fk", baseTableName: "pending_change", constraintName: "FK6vw3w0n09roh5vbd8lh1tsi1w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pi_id", referencedTableName: "price_item")
    }
}
