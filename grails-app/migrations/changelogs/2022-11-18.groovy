package changelogs

import de.laser.Org
import de.laser.system.SystemEvent
import java.sql.Timestamp

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1668769531625-1") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "set_date_created", tableName: "system_setting", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1668769531625-2") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "ddc_date_created", tableName: "dewey_decimal_classification", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1668769531625-3") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "ftc_date_created", tableName: "ftcontrol", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1668769531625-4") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "grs_date_created", tableName: "global_record_source", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1668769531625-5") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "id_date_created", tableName: "identifier", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1668769531625-6") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "igi_date_created", tableName: "issue_entitlement_group_item", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1668769531625-7") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "lang_date_created", tableName: "language", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1668769531625-8") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "sa_date_created", tableName: "system_announcement", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-9") {
        grailsChange {
            change {
                sql.execute("update elasticsearch_source set ess_date_created = now() where ess_date_created is null")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1668769531625-10") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "ess_date_created", tableName: "elasticsearch_source", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-11") {
        grailsChange {
            change {
                sql.execute("update change_notification_queue_item set cnqi_date_created = cnqi_ts where cnqi_date_created is null")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1668769531625-12") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "cnqi_date_created", tableName: "change_notification_queue_item", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-13") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "dc_date_created", tableName: "doc_context", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-14") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "surre_date_created", tableName: "survey_result", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-15") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "tsk_date_created", tableName: "task", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-16") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "lp_date_created", tableName: "license_property", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-17") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "pp_date_created", tableName: "person_property", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-18") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "sp_date_created", tableName: "subscription_property", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1668769531625-19") {
        grailsChange {
            change {
                sql.execute("update pending_change set pc_date_created = pc_ts where pc_date_created is null")
            }
            rollback {}
        }
    }

    /*
    changeSet(author: "klober (modified)", id: "1668769531625-20") {
        grailsChange {
            change {
                List<String> objList = [
                        'AccessPointData',
                        'Address',
                        'BudgetCode',
                        'Combo',
                        'Contact',
                        'Invoice',
                        'Order',
                        'OrgProperty',
                        'OrgSetting',
                        'Person',
                        'PersonRole',
                        'PropertyDefinitionGroup',
                        'PropertyDefinitionGroupBinding',
                        'PropertyDefinitionGroupItem',
                        'TitleHistoryEvent'
                ]

                objList.each { obj ->
                    String nullIdQuery  = 'select obj.id from ' + obj + ' obj where obj.dateCreated is null'
                    String minQuery     = 'select min(dateCreated) from ' + obj
                    String minIdQuery   = 'select obj.id, obj.dateCreated from ' + obj + ' obj where obj.dateCreated = ( ' + minQuery + ' ) order by obj.id asc'
                    String lowerIdQuery_part = 'select obj.id from ' + obj + ' obj where obj.dateCreated is null and obj.id < '

                    List<Long> nullIdList = Org.executeQuery( nullIdQuery )
                    if (nullIdList) {
                        println obj + '.dateCreated=null matches found: ' + nullIdList.size()

                        List<Date> minValue = Org.executeQuery( minQuery )
                        if (minValue) {
                            List<List> minEntry = Org.executeQuery( minIdQuery, [max: 1] )
                            if (minEntry) {
                                println '- min(dateCreated)=' + minEntry[0][1] + ' found @ id=' + minEntry[0][0]

                                List<Long> nullIdBeforeMinList = Org.executeQuery( lowerIdQuery_part + minEntry[0][0] + ' order by obj.id')
                                Timestamp minTs = minEntry[0][1] as Timestamp
                                Timestamp newTs = Timestamp.valueOf(minTs.toLocalDateTime().minusYears(1).withNano(123456000))

                                println '- ' + nullIdBeforeMinList.size() + ' of ' + nullIdList.size() + ' matches with dateCreated = null and id < ' + minEntry[0][0]

                                if (nullIdBeforeMinList) {
                                    println '- setting dateCreated to [' + newTs + '] for ids: ' + nullIdBeforeMinList

                                    SystemEvent.createEvent( 'DBM_SCRIPT_INFO', [
                                            msg: 'Data migration (A1) - replacing null values',
                                            field: obj + '.dateCreated',
                                            newValue: newTs.toString(),
                                            numberOfTargets: nullIdBeforeMinList.size(),
                                            targets: nullIdBeforeMinList
                                    ]
                                    )
                                    Org.executeUpdate('update ' + obj + ' obj set obj.dateCreated = :dc where obj.id in (:idList)', [dc: newTs, idList: nullIdBeforeMinList])
                                }
                            }
                        }
                    }
                }
            }
            rollback {}
        }
    }

    changeSet(author: "klober (generated)", id: "1669018812496-21") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "adr_date_created", tableName: "address", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-22") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "apd_date_created", tableName: "access_point_data", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-23") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "bc_date_created", tableName: "budget_code", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-24") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "combo_date_created", tableName: "combo", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-25") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "ct_date_created", tableName: "contact", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-26") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "the_date_created", tableName: "title_history_event", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-27") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "inv_date_created", tableName: "invoice", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-28") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "op_date_created", tableName: "org_property", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-29") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "ord_date_created", tableName: "ordering", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-30") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "os_date_created", tableName: "org_setting", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-31") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "pc_date_created", tableName: "pending_change", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-32") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "pde_date_created", tableName: "property_definition_group_item", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-33") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "pdg_date_created", tableName: "property_definition_group", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-34") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "pdgb_date_created", tableName: "property_definition_group_binding", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-35") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "pr_date_created", tableName: "person_role", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-36") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "prs_date_created", tableName: "person", validate: "true")
    }

    changeSet(author: "klober (modified)", id: "1669018812496-37") {
        grailsChange {
            change {
                List<String> objList = [
                        'AlternativeName',
                        'IdentifierNamespace',
                        'OrgRole',
                        'PlatformProperty',
                        'RefdataCategory',
                        'RefdataValue',
                        'TIPPCoverage',
                        'TitleHistoryEventParticipant',
                        'UserSetting'
                ]

                objList.each { obj ->
                    String nullIdQuery  = 'select obj.id from ' + obj + ' obj where obj.dateCreated is null'
                    String minQuery     = 'select min(dateCreated) from ' + obj
                    String minIdQuery   = 'select obj.id, obj.dateCreated from ' + obj + ' obj where obj.dateCreated = ( ' + minQuery + ' ) order by obj.id asc'
                    String lowerIdQuery_part  = 'select obj.id from ' + obj + ' obj where obj.dateCreated is null and obj.id < '

                    List<Long> nullIdList = Org.executeQuery( nullIdQuery )
                    if (nullIdList) {
                        println obj + '.dateCreated=null matches found: ' + nullIdList.size()

                        List<Date> minValue = Org.executeQuery( minQuery )
                        if (minValue) {
                            List<List> minEntry = Org.executeQuery( minIdQuery, [max: 1] )
                            if (minEntry) {
                                println '- min(dateCreated)=' + minEntry[0][1] + ' found @ id=' + minEntry[0][0]

                                List<Long> nullIdBeforeMinList = Org.executeQuery( lowerIdQuery_part + minEntry[0][0] + ' order by obj.id')
                                Timestamp minTs = minEntry[0][1] as Timestamp
                                Timestamp newTs = Timestamp.valueOf(minTs.toLocalDateTime().minusYears(1).withNano(123456000))

                                println '- ' + nullIdBeforeMinList.size() + ' of ' + nullIdList.size() + ' matches with dateCreated = null and id < ' + minEntry[0][0]

                                if (nullIdBeforeMinList) {
                                    println '- setting dateCreated to [' + newTs + '] for ids: ' + nullIdBeforeMinList

                                    SystemEvent.createEvent( 'DBM_SCRIPT_INFO', [
                                            msg: 'Data migration (B1) - replacing null values',
                                            field: obj + '.dateCreated',
                                            newValue: newTs.toString(),
                                            numberOfTargets: nullIdBeforeMinList.size(),
                                            targets: nullIdBeforeMinList
                                    ])
                                    Org.executeUpdate('update ' + obj + ' obj set obj.dateCreated = :dc where obj.id in (:idList)', [dc: newTs, idList: nullIdBeforeMinList])
                                }

                                List<Long> nullIdLeftoverList = nullIdList - nullIdBeforeMinList
                                newTs = Timestamp.valueOf(minTs.toLocalDateTime().withNano(654321000))

                                println '- ' + nullIdLeftoverList.size() + ' of ' + nullIdList.size() + ' matches with dateCreated = null and id > ' + minEntry[0][0]

                                if (nullIdLeftoverList) {
                                    println '- setting dateCreated to [' + newTs + '] for ids: ' + nullIdLeftoverList

                                    SystemEvent.createEvent( 'DBM_SCRIPT_INFO', [
                                            msg: 'Data migration (B2) - replacing null values',
                                            field: obj + '.dateCreated',
                                            newValue: newTs.toString(),
                                            numberOfTargets: nullIdLeftoverList.size(),
                                            targets: nullIdLeftoverList
                                    ])
                                    Org.executeUpdate('update ' + obj + ' obj set obj.dateCreated = :dc where obj.id in (:idList)', [dc: newTs, idList: nullIdLeftoverList])
                                }
                            }
                        }
                    }
                }
            }
            rollback {}
        }
    }

    changeSet(author: "klober (generated)", id: "1669018812496-38") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "altname_date_created", tableName: "alternative_name", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-39") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "us_date_created", tableName: "user_setting", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-40") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "idns_date_created", tableName: "identifier_namespace", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-41") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "or_date_created", tableName: "org_role", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-42") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "plp_date_created", tableName: "platform_property", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-43") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "rdc_date_created", tableName: "refdata_category", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-44") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "rdv_date_created", tableName: "refdata_value", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-45") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "tc_date_created", tableName: "tippcoverage", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1669018812496-46") {
        addNotNullConstraint(columnDataType: "timestamp", columnName: "thep_date_created", tableName: "title_history_event_participant", validate: "true")
    }
    */
}
