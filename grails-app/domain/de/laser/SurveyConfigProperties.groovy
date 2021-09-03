package de.laser

import de.laser.properties.PropertyDefinition

class SurveyConfigProperties {

    SurveyConfig surveyConfig
    PropertyDefinition surveyProperty
    boolean mandatoryProperty = false

    Date dateCreated
    Date lastUpdated

    static constraints = {

    }
    static mapping = {
        id column: 'surconpro_id'
        version column: 'surconpro_version'

        dateCreated column: 'surconpro_date_created'
        lastUpdated column: 'surconpro_last_updated'

        mandatoryProperty  column: 'surconpro_mandatory_property'

        surveyConfig column: 'surconpro_survey_config_fk'
        surveyProperty column: 'surconpro_survey_property_fk'
    }
}
