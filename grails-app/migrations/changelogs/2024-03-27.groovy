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

    changeSet(author: "djebeniani (hand-coded)", id: "1711553228628-2") {
        grailsChange {
            change {
                sql.execute("update survey_config_properties set surconpro_property_order = 0")
            }
            rollback {}
        }
    }
}
