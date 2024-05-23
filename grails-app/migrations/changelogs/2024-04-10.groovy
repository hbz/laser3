package changelogs

import de.laser.storage.PropertyStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigProperties

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1713791564528-1") {
        createTable(tableName: "survey_config_package") {
            column(autoIncrement: "true", name: "surconpkg_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_config_packagePK")
            }

            column(name: "surconpkg_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surconpkg_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surconpkg_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surconpkg_pkg_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surconpkg_survey_config_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-2") {
        createTable(tableName: "survey_package_result") {
            column(autoIncrement: "true", name: "surpkgre_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_package_resultPK")
            }

            column(name: "surpkgre_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surpkgre_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surpkgre_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surpkgre_owner_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surpkgre_pkg_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surpkgre_survey_config_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surpkgre_comment", type: "TEXT")

            column(name: "surpkgre_owner_comment", type: "TEXT")

            column(name: "surpkgre_participant_comment", type: "TEXT")

            column(name: "surpkgre_participant_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-3") {
        addColumn(tableName: "cost_item") {
            column(name: "ci_pkg_fk", type: "int8")
        }
    }


    changeSet(author: "djebeniani (generated)", id: "1713791564528-4") {
        createIndex(indexName: "surconpkg_pkg_idx", tableName: "survey_config_package") {
            column(name: "surconpkg_pkg_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-5") {
        createIndex(indexName: "surconpkg_survey_config_idx", tableName: "survey_config_package") {
            column(name: "surconpkg_survey_config_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-6") {
        createIndex(indexName: "surpkgre_owner_idx", tableName: "survey_package_result") {
            column(name: "surpkgre_owner_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-7") {
        createIndex(indexName: "surpkgre_participant_idx", tableName: "survey_package_result") {
            column(name: "surpkgre_participant_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-8") {
        createIndex(indexName: "surpkgre_pkg_idx", tableName: "survey_package_result") {
            column(name: "surpkgre_pkg_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-9") {
        createIndex(indexName: "surpkgre_survey_config_idx", tableName: "survey_package_result") {
            column(name: "surpkgre_survey_config_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-10") {
        addForeignKeyConstraint(baseColumnNames: "surconpkg_survey_config_fk", baseTableName: "survey_config_package", constraintName: "FK15ov4h45c58s85ydc4k7mmmlb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-11") {
        addForeignKeyConstraint(baseColumnNames: "surconpkg_pkg_fk", baseTableName: "survey_config_package", constraintName: "FK664d6t3yo51oa579k98krxo8b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pkg_id", referencedTableName: "package", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-12") {
        addForeignKeyConstraint(baseColumnNames: "surpkgre_survey_config_fk", baseTableName: "survey_package_result", constraintName: "FK77imcsh8vdead91lf25n1lsw9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-13") {
        addForeignKeyConstraint(baseColumnNames: "surpkgre_pkg_fk", baseTableName: "survey_package_result", constraintName: "FKa3go9pkvn7p9f0e3gfx86jmj6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pkg_id", referencedTableName: "package", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-14") {
        addForeignKeyConstraint(baseColumnNames: "surpkgre_owner_fk", baseTableName: "survey_package_result", constraintName: "FKb57xnhdi1ws76k8b5yupawv9i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-15") {
        addForeignKeyConstraint(baseColumnNames: "surpkgre_participant_fk", baseTableName: "survey_package_result", constraintName: "FKj1a96w1no5jwaawt878v0rufj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-16") {
        addForeignKeyConstraint(baseColumnNames: "ci_pkg_fk", baseTableName: "cost_item", constraintName: "FKkodx3m4linmjg2c6n4192mpvo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pkg_id", referencedTableName: "package", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-17") {
        addNotNullConstraint(columnDataType: "int", columnName: "surconpro_property_order", tableName: "survey_config_properties", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1713791564528-18") {
        addColumn(tableName: "survey_config") {
            column(name: "surconf_package_survey", type: "boolean") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1713791564528-19") {
        grailsChange {
            change {
                sql.execute("update survey_config set surconf_package_survey = false")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1713791564528-20") {
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

