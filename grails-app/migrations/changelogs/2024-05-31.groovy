package changelogs

import de.laser.storage.PropertyStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigProperties

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1717188095793-1") {
        addNotNullConstraint(columnDataType: "int", columnName: "cmc_config_order", tableName: "click_me_config", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-2") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "sub_reminder_sent", tableName: "subscription", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-3") {
        createTable(tableName: "survey_config_vendor") {
            column(autoIncrement: "true", name: "surconven_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_config_vendorPK")
            }

            column(name: "surconven_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surconven_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surconven_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surconven_vendor_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surconven_survey_config_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-4") {
        createTable(tableName: "survey_vendor_result") {
            column(autoIncrement: "true", name: "survenre_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_vendor_resultPK")
            }

            column(name: "survenre_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "survenre_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "survenre_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "survenre_vendor_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "survenre_owner_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "survenre_survey_config_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "survenre_comment", type: "TEXT")

            column(name: "survenre_owner_comment", type: "TEXT")

            column(name: "survenre_participant_comment", type: "TEXT")

            column(name: "survenre_participant_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-5") {
        addColumn(tableName: "survey_config") {
            column(name: "surconf_vendor_survey", type: "boolean") {
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-6") {
        createIndex(indexName: "surconven_survey_config_idx", tableName: "survey_config_vendor") {
            column(name: "surconven_survey_config_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-7") {
        createIndex(indexName: "surconven_vendor_idx", tableName: "survey_config_vendor") {
            column(name: "surconven_vendor_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-8") {
        createIndex(indexName: "survenre_owner_idx", tableName: "survey_vendor_result") {
            column(name: "survenre_owner_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-9") {
        createIndex(indexName: "survenre_participant_idx", tableName: "survey_vendor_result") {
            column(name: "survenre_participant_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-10") {
        createIndex(indexName: "survenre_survey_config_idx", tableName: "survey_vendor_result") {
            column(name: "survenre_survey_config_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-11") {
        createIndex(indexName: "survenre_vendor_idx", tableName: "survey_vendor_result") {
            column(name: "survenre_vendor_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-12") {
        addForeignKeyConstraint(baseColumnNames: "survenre_owner_fk", baseTableName: "survey_vendor_result", constraintName: "FK1t0756c4kgcpdfh05462o52tj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-13") {
        addForeignKeyConstraint(baseColumnNames: "survenre_participant_fk", baseTableName: "survey_vendor_result", constraintName: "FK7om8a3brfjk8kh4m0ejrernlv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-14") {
        addForeignKeyConstraint(baseColumnNames: "survenre_survey_config_fk", baseTableName: "survey_vendor_result", constraintName: "FKasn3a8q3uikwrrapevjg4h286", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-15") {
        addForeignKeyConstraint(baseColumnNames: "surconven_survey_config_fk", baseTableName: "survey_config_vendor", constraintName: "FKef7951aw67a1duifwdqkggjyh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-16") {
        addForeignKeyConstraint(baseColumnNames: "survenre_vendor_fk", baseTableName: "survey_vendor_result", constraintName: "FKmg5xybgle1xfncjyopslair3g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-17") {
        addForeignKeyConstraint(baseColumnNames: "surconven_vendor_fk", baseTableName: "survey_config_vendor", constraintName: "FKsfuu4rx3nc53tvfgc9a1s2xbe", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1717188095793-18") {
        grailsChange {
            change {
                sql.execute("update survey_config set surconf_package_survey = false")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1717188095793-19") {
        grailsChange {
            change {
                sql.execute("update survey_config set surconf_vendor_survey = false")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1717188095793-20") {
        grailsChange {
            change {
                sql.execute("update survey_config set surconf_invoicing_information = false")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-21") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "surconf_vendor_survey", tableName: "survey_config", validate: "true")
    }


    changeSet(author: "djebeniani (generated)", id: "1717188095793-22") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "surconf_invoicing_information", tableName: "survey_config", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1717188095793-23") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "surconf_package_survey", tableName: "survey_config", validate: "true")
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1717188095793-24") {
        grailsChange {
            change {
                SurveyConfig.withTransaction {
                    SurveyConfig.findAll().each { SurveyConfig surveyConfig ->
                        LinkedHashSet<SurveyConfigProperties> propertiesParticipation = []
                        LinkedHashSet<SurveyConfigProperties> propertiesMandatory = []
                        LinkedHashSet<SurveyConfigProperties> propertiesNoMandatory = []

                        surveyConfig.surveyProperties.each {
                            if (it.surveyProperty == PropertyStore.SURVEY_PROPERTY_PARTICIPATION) {
                                propertiesParticipation << it
                            } else if (it.mandatoryProperty == true && it.surveyProperty != PropertyStore.SURVEY_PROPERTY_PARTICIPATION) {
                                propertiesMandatory << it
                            } else if (it.mandatoryProperty == false && it.surveyProperty != PropertyStore.SURVEY_PROPERTY_PARTICIPATION) {
                                propertiesNoMandatory << it
                            }
                        }

                        propertiesParticipation = propertiesParticipation.sort { it.surveyProperty.name_de }

                        propertiesMandatory = propertiesMandatory.sort { it.surveyProperty.name_de }

                        propertiesNoMandatory = propertiesNoMandatory.sort { it.surveyProperty.name_de }

                        int count = 0
                        propertiesParticipation.eachWithIndex { SurveyConfigProperties surveyConfigProperties, int i ->
                            count = count+1
                            surveyConfigProperties.propertyOrder = count
                            surveyConfigProperties.save()

                        }

                        propertiesMandatory.eachWithIndex { SurveyConfigProperties surveyConfigProperties, int i ->
                            count = count+1
                            surveyConfigProperties.propertyOrder = count
                            surveyConfigProperties.save()

                        }

                        propertiesNoMandatory.eachWithIndex { SurveyConfigProperties surveyConfigProperties, int i ->
                            count = count+1
                            surveyConfigProperties.propertyOrder = count
                            surveyConfigProperties.save()

                        }

                    }
                }
            }
        }
    }

}
