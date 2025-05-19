package changelogs

import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyPersonResult

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1739806204859-1") {
        createTable(tableName: "survey_person_result") {
            column(autoIncrement: "true", name: "surprere_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_person_resultPK")
            }

            column(name: "surprere_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surprere_person_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surprere_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surprere_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surprere_owner_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surprere_survey_config_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surprere_participant_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surprere_billing_person", type: "boolean") {
            }

            column(name: "surprere_survey_person", type: "boolean") {
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1739806204859-2") {
        createIndex(indexName: "surprere_owner_idx", tableName: "survey_person_result") {
            column(name: "surprere_owner_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1739806204859-3") {
        createIndex(indexName: "surprere_participant_idx", tableName: "survey_person_result") {
            column(name: "surprere_participant_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1739806204859-4") {
        createIndex(indexName: "surprere_person_idx", tableName: "survey_person_result") {
            column(name: "surprere_person_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1739806204859-5") {
        createIndex(indexName: "surprere_survey_config_idx", tableName: "survey_person_result") {
            column(name: "surprere_survey_config_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1739806204859-6") {
        addForeignKeyConstraint(baseColumnNames: "surprere_person_fk", baseTableName: "survey_person_result", constraintName: "FKg1rdm1cguel9pls6m5ooq8xcg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prs_id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1739806204859-7") {
        addForeignKeyConstraint(baseColumnNames: "surprere_survey_config_fk", baseTableName: "survey_person_result", constraintName: "FKs7jj5sgru1sgjh98xpt3208rw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1739806204859-8") {
        addForeignKeyConstraint(baseColumnNames: "surprere_owner_fk", baseTableName: "survey_person_result", constraintName: "FKsdurt93au7ip5jwb4k7k8mj56", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1739806204859-9") {
        addForeignKeyConstraint(baseColumnNames: "surprere_participant_fk", baseTableName: "survey_person_result", constraintName: "FKth9quu3x5ae21myqca9mjf6ob", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1739806204859-10") {
        addColumn(tableName: "person") {
            column(name: "prs_preferred_for_survey", type: "boolean") {
            }
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1739806204859-11") {
        grailsChange {
            change {
                String query = "update person set prs_preferred_for_survey = false;"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1739806204859-12") {
        addColumn(tableName: "address") {
            column(name: "adr_preferred_for_survey", type: "boolean") {
            }
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1739806204859-13") {
        grailsChange {
            change {
                String query = "update address set adr_preferred_for_survey = false;"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1739806204859-14") {
        grailsChange {
            change {
               /*
               Problem by Prod/Test deploy becouase changes on SurveyConfig Domain Class -> moved in 1746769820789-21
               String query = 'select s from SurveyOrg s where person is not null'
                Set<SurveyOrg> surveyOrgs = SurveyOrg.executeQuery(query)
                int cnt = 0
                int cntSurOrgPer = surveyOrgs.size()
                surveyOrgs.each { SurveyOrg surveyOrg ->
                    SurveyPersonResult surveyPersonResult = new SurveyPersonResult(surveyConfig: surveyOrg.surveyConfig, owner: surveyOrg.surveyConfig.surveyInfo.owner, participant: surveyOrg.org, person: surveyOrg.person, billingPerson: true)
                    if(!surveyPersonResult.save()) {
                        println surveyPersonResult.errors.getAllErrors().toListString()
                    }else {
                        cnt++
                    }
                }
                confirm("${query}: ${cnt}/${cntSurOrgPer}")
                changeSet.setComments("${query}: ${cnt}/${cntSurOrgPer}")*/
            }
            rollback {}
        }
    }

}
