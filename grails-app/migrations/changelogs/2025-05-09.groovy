package changelogs

import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyPersonResult

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1746769820789-1") {
        createTable(tableName: "survey_config_subscription") {
            column(autoIncrement: "true", name: "surconsub_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_config_subscriptionPK")
            }

            column(name: "surconsub_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surconsub_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surconsub_subscription_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surconsub_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surconsub_survey_config_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-2") {
        createTable(tableName: "survey_subscription_result") {
            column(autoIncrement: "true", name: "sursubre_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_subscription_resultPK")
            }

            column(name: "sursubre_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sursubre_comment", type: "TEXT")

            column(name: "sursubre_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "sursubre_subscription_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sursubre_owner_comment", type: "TEXT")

            column(name: "sursubre_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "sursubre_owner_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sursubre_participant_comment", type: "TEXT")

            column(name: "sursubre_survey_config_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "sursubre_participant_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-3") {
        addColumn(tableName: "cost_item") {
            column(name: "ci_surveyconfigsubscription_fk", type: "int8")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-4") {
        addColumn(tableName: "survey_config") {
            column(name: "surconf_subscription_survey", type: "boolean") {
            }
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1746769820789-5") {
        grailsChange {
            change {
                sql.execute("update survey_config set surconf_subscription_survey = false")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-6") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "surconf_subscription_survey", tableName: "survey_config", validate: "true")
    }


    changeSet(author: "djebeniani (generated)", id: "1746769820789-7") {
        createIndex(indexName: "ci_surveyconfigsubscription_idx", tableName: "cost_item") {
            column(name: "ci_surveyconfigsubscription_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-8") {
        createIndex(indexName: "surconsub_subscription_idx", tableName: "survey_config_subscription") {
            column(name: "surconsub_subscription_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-9") {
        createIndex(indexName: "surconsub_survey_config_idx", tableName: "survey_config_subscription") {
            column(name: "surconsub_survey_config_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-10") {
        createIndex(indexName: "sursubre_owner_idx", tableName: "survey_subscription_result") {
            column(name: "sursubre_owner_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-11") {
        createIndex(indexName: "sursubre_participant_idx", tableName: "survey_subscription_result") {
            column(name: "sursubre_participant_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-12") {
        createIndex(indexName: "sursubre_subscription_idx", tableName: "survey_subscription_result") {
            column(name: "sursubre_subscription_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-13") {
        createIndex(indexName: "sursubre_survey_config_idx", tableName: "survey_subscription_result") {
            column(name: "sursubre_survey_config_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-14") {
        addForeignKeyConstraint(baseColumnNames: "sursubre_participant_fk", baseTableName: "survey_subscription_result", constraintName: "FKcy1j04etfw8b20a7no3dvhvyx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-15") {
        addForeignKeyConstraint(baseColumnNames: "sursubre_survey_config_fk", baseTableName: "survey_subscription_result", constraintName: "FKerxagso4fhgxfri86x91ecb5n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-16") {
        addForeignKeyConstraint(baseColumnNames: "surconsub_survey_config_fk", baseTableName: "survey_config_subscription", constraintName: "FKf9opks5f6f8yhx399255ct7pc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-17") {
        addForeignKeyConstraint(baseColumnNames: "ci_surveyconfigsubscription_fk", baseTableName: "cost_item", constraintName: "FKffeq8ymlgk7omveumnu2swjg5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconsub_id", referencedTableName: "survey_config_subscription", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-18") {
        addForeignKeyConstraint(baseColumnNames: "surconsub_subscription_fk", baseTableName: "survey_config_subscription", constraintName: "FKoc3m4a4dg4oearfu3lwcn45gr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-19") {
        addForeignKeyConstraint(baseColumnNames: "sursubre_owner_fk", baseTableName: "survey_subscription_result", constraintName: "FKpstrp1w6l1qenl9nwquw63vgy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1746769820789-20") {
        addForeignKeyConstraint(baseColumnNames: "sursubre_subscription_fk", baseTableName: "survey_subscription_result", constraintName: "FKt6lvgc6kbsf3jh5rcrpsidl5h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", validate: "true")
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1746769820789-21") {
        grailsChange {
            change {
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
                changeSet.setComments("${query}: ${cnt}/${cntSurOrgPer}")
            }
            rollback {}
        }
    }
}
