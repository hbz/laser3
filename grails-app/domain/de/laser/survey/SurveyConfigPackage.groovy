package de.laser.survey

import de.laser.wekb.Package

class SurveyConfigPackage {

    static belongsTo = [
            surveyConfig:            SurveyConfig,
            pkg:   Package
    ]

    Date dateCreated
    Date lastUpdated

    static constraints = {

    }

    static mapping = {
        id column: 'surconpkg_id'
        version column: 'surconpkg_version'

        dateCreated column: 'surconpkg_date_created'
        lastUpdated column: 'surconpkg_last_updated'

        surveyConfig column: 'surconpkg_survey_config_fk', index: 'surconpkg_survey_config_idx'
        pkg column: 'surconpkg_pkg_fk', index: 'surconpkg_pkg_idx'

    }
}
