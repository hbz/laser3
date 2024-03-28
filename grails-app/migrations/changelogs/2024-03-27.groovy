package changelogs

import de.laser.storage.PropertyStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigProperties

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1711553228628-1") {
        addColumn(tableName: "survey_config_properties") {
            column(name: "surconpro_property_order", type: "int4")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1711553228628-2") {
        grailsChange {
            change {
                sql.execute("update survey_config_properties set surconpro_property_order = 0")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (modified)", id: "1711553228628-3") {
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
