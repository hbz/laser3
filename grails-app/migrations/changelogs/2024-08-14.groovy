package changelogs

import de.laser.DueDateObject

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1723617251555-1") {
        addColumn(tableName: "due_date_object") {
            column(name: "ddo_property_oid", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-2") {
        addColumn(tableName: "due_date_object") {
            column(name: "ddo_subscription_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-3") {
        addColumn(tableName: "due_date_object") {
            column(name: "ddo_survey_info_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-4") {
        addColumn(tableName: "due_date_object") {
            column(name: "ddo_task_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-5") {
        addForeignKeyConstraint(baseColumnNames: "ddo_subscription_fk", baseTableName: "due_date_object", constraintName: "FKi407g2ds7wauntg1dhx18rgxi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1723617251555-6") {
        addForeignKeyConstraint(baseColumnNames: "ddo_survey_info_fk", baseTableName: "due_date_object", constraintName: "FK70o29ja9fo027co114s6ikkfd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surin_id", referencedTableName: "survey_info", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1723617251555-7") {
        addForeignKeyConstraint(baseColumnNames: "ddo_task_fk", baseTableName: "due_date_object", constraintName: "FKfisaip13g3kqwe6ugvs2w1vcr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tsk_id", referencedTableName: "task", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1723617251555-8") {
        grailsChange {
            change {
                String sql = "delete from DueDateObject where oid like 'com.k_int.kbplus.%'"
                int done = DueDateObject.executeUpdate( sql )

                confirm( sql + ' -> ' + done )
                changeSet.setComments( sql + ' -> ' + done )
            }
            rollback {}
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-9") {
        addColumn(tableName: "due_date_object") {
            column(name: "ddo_license_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1723617251555-10") {
        addForeignKeyConstraint(baseColumnNames: "ddo_license_fk", baseTableName: "due_date_object", constraintName: "FK7eiafhdvdli77rg8c76ycw4i7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", validate: "true")
    }

//    changeSet(author: "klober (modified)", id: "1723617251555-11") {
//        grailsChange {
//            change {
//                List done = []
//
//                DueDateObject.findAllByOidLike('de.laser.Subscription$HibernateProxy$%').each { ddo ->
//                    String oid = 'de.laser.Subscription:' + ddo.oid.split(':').last()
//                    DueDateObject.executeUpdate('update DueDateObject set oid = :oid where id = :id', [oid: oid, id: ddo.id])
//                    done << ddo.id
//                }
//                confirm( 'fixed de.laser.Subscription$HibernateProxy entries -> ' + done )
//                changeSet.setComments( 'fixed de.laser.Subscription$HibernateProxy entries -> ' + done.size() )
//            }
//            rollback {}
//        }
//    }

//    changeSet(author: "klober (modified)", id: "1723617251555-xx") {
//        grailsChange {
//            change {
//                Map done    = [license:[], subscription:[], surveyInfo:[], task:[]]
//                Map errors  = [license:[], subscription:[], surveyInfo:[], task:[]]
//
//                GenericOIDService genericOIDService = BeanStore.getGenericOIDService()
//
//                DueDateObject.findAllByOidLike('de.laser.License:%').each { ddo ->
//                    License obj = genericOIDService.resolveOID(ddo.oid) as License
//                    if (obj) {
//                        DueDateObject.executeUpdate('update DueDateObject set license = :obj where id = :id', [obj: obj, id: ddo.id])
//                        done.license << ddo.id
//                    } else {
//                        errors.license << ddo.id
//                    }
//                }
//                DueDateObject.findAllByOidLike('de.laser.Subscription:%').each { ddo ->
//                    Subscription obj = genericOIDService.resolveOID(ddo.oid) as Subscription
//                    if (obj) {
//                        DueDateObject.executeUpdate('update DueDateObject set subscription = :obj where id = :id', [obj: obj, id: ddo.id])
//                        done.subscription << ddo.id
//                    } else {
//                        errors.subscription << ddo.id
//                    }
//                }
//                DueDateObject.findAllByOidLike('de.laser.survey.SurveyInfo:%').each { ddo ->
//                    SurveyInfo obj = genericOIDService.resolveOID(ddo.oid) as SurveyInfo
//                    if (obj) {
//                        DueDateObject.executeUpdate('update DueDateObject set surveyInfo = :obj where id = :id', [obj: obj, id: ddo.id])
//                        done.surveyInfo << ddo.id
//                    } else {
//                        errors.surveyInfo << ddo.id
//                    }
//                }
//                DueDateObject.findAllByOidLike('de.laser.Task:%').each { ddo ->
//                    Task obj = genericOIDService.resolveOID(ddo.oid) as Task
//                    if (obj) {
//                        DueDateObject.executeUpdate('update DueDateObject set task = :obj where id = :id', [obj: obj, id: ddo.id])
//                        done.task << ddo.id
//                    } else {
//                        errors.task << ddo.id
//                    }
//                }
//                confirm( 'ERRORS: ' + errors.toMapString() + ', DONE: ' + done.toMapString() )
//                changeSet.setComments( errors.toMapString() )
//            }
//            rollback {}
//        }
//    }
}
