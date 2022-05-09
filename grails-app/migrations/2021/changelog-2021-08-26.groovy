import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigProperties
import de.laser.survey.SurveyInfo
import de.laser.storage.RDStore

databaseChangeLog = {

    changeSet(author: "galffy (modified)", id: "1629958945001-1") {
        addUniqueConstraint(columnNames: "lsc_customer_fk, lsc_platform_fk, lsc_report_id", constraintName: "lsc_unique_report_per_customer", tableName: "laser_stats_cursor")
    }

    changeSet(author: "djebeniani (generated)", id: "1629958945001-2") {
        addColumn(tableName: "survey_config_properties") {
            column(name: "surconpro_mandatory_property", type: "boolean") {
            }
        }
    }

    changeSet(author: "djebeniani (modified)", id: "1629958945001-3") {
        grailsChange {
            change {
                sql.execute('update survey_config_properties set surconpro_mandatory_property = false where surconpro_mandatory_property is null')
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (modified)", id: "1629958945001-4") {
        grailsChange {
            change {
                SurveyInfo.findAllByTypeAndStatusNotInList(RDStore.SURVEY_TYPE_RENEWAL, [RDStore.SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION]).each { SurveyInfo surveyInfo ->
                    surveyInfo.surveyConfigs.each { SurveyConfig surveyConfig ->
                        SurveyConfigProperties surveyConfigProperties = SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, RDStore.SURVEY_PROPERTY_PARTICIPATION)
                        if(surveyConfigProperties){
                            surveyConfigProperties.mandatoryProperty = true
                            surveyConfigProperties.save(flush: true)
                        }
                    }
                }
            }
            rollback {}
        }
    }


}
